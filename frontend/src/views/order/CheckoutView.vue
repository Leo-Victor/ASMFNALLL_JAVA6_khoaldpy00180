<script setup>
import {onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {CheckoutPage} from "@/legacy/pages";

const {checkout, form, result, error, submit, money} = CheckoutPage.setup();
const router = useRouter();
const province = "Gia Lai";
const wards = [
    "Phường Quy Nhơn",
    "Phường Quy Nhơn Đông",
    "Phường Quy Nhơn Tây",
    "Phường Quy Nhơn Nam",
    "Phường Quy Nhơn Bắc",
    "Xã Nhơn Châu"
];
if (!form.address) {
    form.address = "";
}
const selectedWard = ref("");
const mapRef = ref(null);
const geocodeMessage = ref("");
const placing = ref(false);
let leafletMap = null;
let leafletMarker = null;
let geocodeTimer = null;
let geocodeAbort = null;
const onWardChange = (ward) => {
    selectedWard.value = ward;
    form.address = `${ward}, ${province}`;
};
const ensureLeaflet = async () => {
    if (window.L) {
        return window.L;
    }
    await new Promise((resolve, reject) => {
        const css = document.createElement("link");
        css.rel = "stylesheet";
        css.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
        document.head.appendChild(css);
        const script = document.createElement("script");
        script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
        script.onload = resolve;
        script.onerror = reject;
        document.body.appendChild(script);
    });
    return window.L;
};
const requestJsonp = (query) => {
    return new Promise((resolve, reject) => {
        const callbackName = `nominatim_jsonp_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
        const script = document.createElement("script");
        script.src = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&json_callback=${callbackName}`;
        script.onerror = () => {
            delete window[callbackName];
            if (script.parentNode) {
                script.parentNode.removeChild(script);
            }
            reject(new Error("jsonp-error"));
        };
        window[callbackName] = (payload) => {
            resolve(payload);
            delete window[callbackName];
            if (script.parentNode) {
                script.parentNode.removeChild(script);
            }
        };
        document.body.appendChild(script);
        setTimeout(() => {
            if (window[callbackName]) {
                delete window[callbackName];
                if (script.parentNode) {
                    script.parentNode.removeChild(script);
                }
                reject(new Error("jsonp-timeout"));
            }
        }, 4500);
    });
};
const applyGeoResult = (json) => {
    if (!Array.isArray(json) || !json.length) {
        return false;
    }
    const lat = Number(json[0].lat);
    const lng = Number(json[0].lon);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        return false;
    }
    form.lat = lat.toFixed(6);
    form.lng = lng.toFixed(6);
    geocodeMessage.value = "";
    if (leafletMarker) {
        leafletMarker.setLatLng([lat, lng]);
    } else if (leafletMap) {
        leafletMarker = window.L.marker([lat, lng]).addTo(leafletMap);
    }
    if (leafletMap) {
        leafletMap.setView([lat, lng], 14);
    }
    return true;
};
const geocodeAddress = async () => {
    if (!selectedWard.value) {
        return;
    }
    const queryList = [
        `${selectedWard.value}, ${province}, Việt Nam`,
        `${selectedWard.value}, Bình Định, Việt Nam`,
        `${selectedWard.value}, Việt Nam`
    ];
    if (geocodeAbort) {
        geocodeAbort.abort();
    }
    geocodeAbort = new AbortController();
    try {
        for (const query of queryList) {
            const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`;
            const res = await fetch(url, {
                headers: {"Accept-Language": "vi"},
                signal: geocodeAbort.signal
            });
            const json = await res.json();
            if (applyGeoResult(json)) {
                return;
            }
        }
        for (const query of queryList) {
            try {
                const jsonpResult = await requestJsonp(query);
                if (applyGeoResult(jsonpResult)) {
                    return;
                }
            } catch (e) {
            }
        }
        geocodeMessage.value = "Không tìm thấy vị trí bản đồ cho địa chỉ đã chọn qua tất cả nhánh fallback.";
    } catch (e) {
        if (e?.name === "AbortError") {
            return;
        }
        geocodeMessage.value = "Không thể định vị địa chỉ lúc này.";
    }
};
watch(selectedWard, () => {
    if (geocodeTimer) {
        clearTimeout(geocodeTimer);
    }
    geocodeTimer = setTimeout(geocodeAddress, 400);
});
onMounted(async () => {
    try {
        const L = await ensureLeaflet();
        if (!mapRef.value) {
            return;
        }
        leafletMap = L.map(mapRef.value).setView([10.7769, 106.7009], 13);
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19
        }).addTo(leafletMap);
        leafletMap.on("click", (event) => {
            const {lat, lng} = event.latlng;
            if (leafletMarker) {
                leafletMarker.setLatLng([lat, lng]);
            } else {
                leafletMarker = L.marker([lat, lng]).addTo(leafletMap);
            }
            form.lat = lat.toFixed(6);
            form.lng = lng.toFixed(6);
            geocodeMessage.value = "";
        });
    } catch (e) {
        geocodeMessage.value = "Không tải được bản đồ Leaflet.";
    }
});
const submitCheckout = async () => {
    placing.value = true;
    await submit();
    placing.value = false;
    const orderId = result.value?.data?.orderId;
    const nextAction = result.value?.data?.nextAction;
    if (!orderId) {
        return;
    }
    if (nextAction === "BANK_TRANSFER" || (form.paymentMethod || "").toUpperCase() === "BANK") {
        await router.push(`/order/bank-transfer?id=${orderId}`);
        return;
    }
    await router.push(`/order/order-detail?id=${orderId}`);
};
</script>

<template>
    <main class="checkout-page">
        <div class="container">
            <h1 class="page-title">Thanh toán đơn hàng</h1>
            
            <div v-if="error" class="status-message status-error">{{ error }}</div>
            
            <div class="checkout-layout">
                <div class="checkout-main">
                    <div class="checkout-section">
                        <h3 class="checkout-section-title">Thông tin đơn hàng</h3>
                        <div class="order-items-card">
                            <table class="order-items-table">
                                <thead>
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th>Giá</th>
                                        <th>SL</th>
                                        <th>Size</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="item in checkout.items" :key="item.productId + '-' + item.sizeId">
                                        <td>{{ item.name }}</td>
                                        <td>{{ money(item.price) }} VNĐ</td>
                                        <td>{{ item.quantity }}</td>
                                        <td>{{ item.sizeName }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <form class="checkout-section" @submit.prevent="submitCheckout">
                        <h3 class="checkout-section-title">Địa chỉ giao hàng</h3>
                        
                        <div class="form-group">
                            <label>Tỉnh / Thành phố</label>
                            <select :value="province" disabled class="form-control">
                                <option>{{ province }}</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label>Phường / Xã</label>
                            <select @change="onWardChange($event.target.value)" required class="form-control">
                                <option value="">Chọn phường/xã</option>
                                <option v-for="ward in wards" :key="ward" :value="ward">{{ ward }}</option>
                            </select>
                        </div>
                        
                        <div class="checkout-map" ref="mapRef"></div>
                        
                        <div v-if="geocodeMessage" class="status-message status-error">{{ geocodeMessage }}</div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label>Latitude</label>
                                <input v-model="form.lat" class="form-control" readonly>
                            </div>
                            <div class="form-group">
                                <label>Longitude</label>
                                <input v-model="form.lng" class="form-control" readonly>
                            </div>
                        </div>
                        
                        <div class="checkout-payment-group">
                            <label>Hình thức thanh toán</label>
                            <div class="checkout-payment-options">
                                <label class="checkout-payment-option">
                                    <input type="radio" value="BANK" v-model="form.paymentMethod" class="checkout-payment-radio">
                                    <span>Chuyển khoản ngân hàng</span>
                                </label>
                                <label class="checkout-payment-option">
                                    <input type="radio" value="COD" v-model="form.paymentMethod" class="checkout-payment-radio">
                                    <span>Thanh toán khi nhận hàng (COD)</span>
                                </label>
                            </div>
                        </div>
                        
                        <button class="btn btn-primary btn--block" type="submit" :disabled="placing">{{ placing ? "Đang xử lý..." : "Đặt hàng ngay" }}</button>
                    </form>
                    
                    <div v-if="result" class="status-message status-success">
                        Đặt hàng thành công! Mã đơn hàng: {{ result.data?.orderId }}
                    </div>
                </div>
                
                <div class="checkout-sidebar">
                    <div class="order-summary-card">
                        <h3 class="order-summary-title">Tổng đơn hàng</h3>
                        <div class="order-summary-row">
                            <span>Tạm tính:</span>
                            <strong>{{ money(checkout.totalPrice) }} VNĐ</strong>
                        </div>
                        <div class="order-summary-row">
                            <span>Phí vận chuyển:</span>
                            <strong>Miễn phí</strong>
                        </div>
                        <div class="order-summary-divider"></div>
                        <div class="order-summary-total">
                            <span>Tổng cộng:</span>
                            <strong>{{ money(checkout.totalPrice) }} VNĐ</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>
