<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {AdminOrderPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {rows, selected, status, payosCode, msg, detail: fetchDetail, updateStatus, cancelPayos} = AdminOrderPage.setup();
const route = useRoute();
const mapRef = ref(null);
const detailModalOpen = ref(false);
let map = null;
let marker = null;
let destMarker = null;
let routeLine = null;
let simulateTimer = null;
const statusLabel = (value) => {
    const labels = {
        PLACED_UNPAID: "Đã đặt - chưa thanh toán",
        PLACED_PAID: "Đã đặt - đã thanh toán",
        SHIPPING_UNPAID: "Đang giao - chưa thanh toán",
        SHIPPING_PAID: "Đang giao - đã thanh toán",
        DELIVERED_SUCCESS: "Giao hàng thành công",
        DELIVERY_FAILED: "Giao hàng thất bại"
    };
    return labels[value] || value || "Không rõ";
};
const isDelivered = (value) => value === "DELIVERED_SUCCESS" || value === "DONE";
const statusColor = (value) => isDelivered(value) ? "#64d441" : "#b62c54";
const statusOptions = computed(() => {
    const current = selected.value?.order?.status || "";
    if (current === "PLACED_UNPAID") {
        return [{value: "SHIPPING_UNPAID", label: "Đang giao - chưa TT"}];
    }
    if (current === "PLACED_PAID") {
        return [{value: "SHIPPING_PAID", label: "Đang giao - đã TT"}];
    }
    if (current === "SHIPPING_UNPAID") {
        return [
            {value: "SHIPPING_UNPAID", label: "Đang giao - chưa TT"},
            {value: "DELIVERED_SUCCESS", label: "Giao thành công"},
            {value: "DELIVERY_FAILED", label: "Giao thất bại"}
        ];
    }
    if (current === "SHIPPING_PAID") {
        return [
            {value: "SHIPPING_PAID", label: "Đang giao - đã TT"},
            {value: "DELIVERED_SUCCESS", label: "Giao thành công"},
            {value: "DELIVERY_FAILED", label: "Giao thất bại"}
        ];
    }
    if (isDelivered(current)) {
        return [{value: current, label: "Giao thành công"}];
    }
    if (current === "DELIVERY_FAILED" || current === "CANCEL") {
        return [{value: current, label: "Giao thất bại"}];
    }
    return [{value: current || "PLACED_UNPAID", label: statusLabel(current || "PLACED_UNPAID")}];
});
const canUpdateStatus = computed(() => !isDelivered(selected.value?.order?.status || ""));
const openDetail = async (id) => {
    await fetchDetail(id);
    const firstOption = statusOptions.value[0]?.value;
    if (firstOption && !statusOptions.value.some((item) => item.value === status.value)) {
        status.value = firstOption;
    }
    detailModalOpen.value = true;
    await nextTick();
    await initMap();
};
const closeDetailModal = () => {
    detailModalOpen.value = false;
    clearSimulation();
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
const clearSimulation = () => {
    if (simulateTimer) {
        clearInterval(simulateTimer);
        simulateTimer = null;
    }
};
const initMap = async () => {
    if (!selected.value || !mapRef.value) {
        return;
    }
    const L = await ensureLeaflet();
    const store = [13.779876, 109.228232];
    const lat = Number(selected.value.deliveryLat || 10.7769);
    const lng = Number(selected.value.deliveryLng || 106.7009);
    const dest = [lat, lng];
    if (!map) {
        map = L.map(mapRef.value).setView([(store[0] + dest[0]) / 2, (store[1] + dest[1]) / 2], 12);
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {maxZoom: 19}).addTo(map);
    } else {
        map.invalidateSize();
    }
    if (routeLine) {
        map.removeLayer(routeLine);
    }
    if (marker) {
        map.removeLayer(marker);
    }
    if (destMarker) {
        map.removeLayer(destMarker);
    }
    routeLine = L.polyline([store, dest], {color: "#2563eb", weight: 4}).addTo(map);
    marker = L.marker(store).addTo(map).bindPopup("Cửa hàng");
    destMarker = L.marker(dest).addTo(map).bindPopup("Điểm giao");
    map.fitBounds(routeLine.getBounds(), {padding: [20, 20]});
    clearSimulation();
    if ((status.value || "").startsWith("SHIPPING")) {
        const points = routeLine.getLatLngs();
        let step = 0;
        simulateTimer = setInterval(async () => {
            step += 1;
            if (step >= 30) {
                clearSimulation();
                status.value = "DELIVERED_SUCCESS";
                await updateStatus();
                return;
            }
            const latNow = points[0].lat + (points[1].lat - points[0].lat) * (step / 30);
            const lngNow = points[0].lng + (points[1].lng - points[0].lng) * (step / 30);
            marker.setLatLng([latNow, lngNow]);
        }, 500);
    }
};
watch(selected, async () => {
    const firstOption = statusOptions.value[0]?.value;
    if (firstOption && !statusOptions.value.some((item) => item.value === status.value)) {
        status.value = firstOption;
    }
    if (!detailModalOpen.value) {
        return;
    }
    await nextTick();
    await initMap();
});
watch(status, async () => {
    if (selected.value && detailModalOpen.value) {
        await initMap();
    }
});
onUnmounted(() => {
    clearSimulation();
    if (map) {
        map.remove();
    }
});
onMounted(async () => {
    const queryOrderId = Number(route.query.orderId || "");
    if (Number.isFinite(queryOrderId) && queryOrderId > 0) {
        await openDetail(queryOrderId);
    }
});
</script>

<template>
    <main class="container admin-product-page">
        <h3 class="page-title">Quản lý đơn hàng</h3>
        <div class="admin-product-shell">
            <div class="admin-product-menu">
                <AdminNav/>
                <div class="card">
                    <h4>Huỷ PayOS</h4>
                    <div v-if="msg" class="status-message">{{ msg }}</div>
                    <div class="form-group">
                        <label>Mã đơn hàng</label>
                        <input type="number" min="1" v-model="payosCode">
                    </div>
                    <button class="btn btn-primary" type="button" @click="cancelPayos">Gửi lệnh huỷ</button>
                </div>
            </div>
            <div class="admin-product-main">
                <div class="card">
                    <table>
                        <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Username</th>
                            <th>Trạng thái</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="o in rows" :key="o.id">
                            <td>{{ o.id }}</td>
                            <td>{{ o.account?.username || "N/A" }}</td>
                            <td><span class="badge" :style="{color: statusColor(o.status)}">{{ statusLabel(o.status) }}</span></td>
                            <td class="table-actions">
                                <button class="btn btn-action-outline" type="button" @click="openDetail(o.id)">Chi tiết</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="modal-backdrop" :class="{open: detailModalOpen}" v-if="detailModalOpen && selected">
            <div class="admin-modal-panel">
                <div class="table-actions" style="justify-content: space-between; margin-bottom: 8px;">
                    <h4 style="margin:0;">Chi tiết đơn hàng</h4>
                    <button class="btn btn-outline-primary" type="button" @click="closeDetailModal">Đóng</button>
                </div>
                <div>Mã đơn: <strong>{{ selected.order.id }}</strong> - Địa chỉ: <span>{{ selected.order.address }}</span></div>
                <div style="margin-top:12px;">
                    <div class="form-group">
                        <label>Trạng thái đơn hàng</label>
                        <select v-model="status">
                            <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                        </select>
                    </div>
                    <button class="btn btn-primary" type="button" @click="updateStatus" v-if="canUpdateStatus">
                        Cập nhật trạng thái
                    </button>
                </div>
                <table style="margin-top:10px;">
                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Giá</th>
                        <th>SL</th>
                        <th>Size</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="d in (selected.details || [])" :key="d.id">
                        <td>{{ d.product?.name }}</td>
                        <td>{{ d.price }}</td>
                        <td>{{ d.quantity }}</td>
                        <td>{{ d.sizeName }}</td>
                    </tr>
                    </tbody>
                </table>
                <div class="card" style="margin-top:12px;">
                    <h4>Bản đồ giao hàng</h4>
                    <div ref="mapRef" style="height:360px;"></div>
                </div>
            </div>
        </div>
    </main>
</template>
