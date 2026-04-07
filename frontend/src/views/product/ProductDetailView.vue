<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ProductDetailPage} from "@/legacy/pages";
import {api} from "@/api";

const {productId, data, error, load, money} = ProductDetailPage.setup();
const route = useRoute();
const router = useRouter();
const loadByQuery = async () => {
    if (typeof window !== "undefined") {
        window.scrollTo({top: 0, left: 0, behavior: "auto"});
    }
    const queryId = Number(route.query.id || route.query.productId || productId.value);
    if (Number.isFinite(queryId) && queryId > 0) {
        productId.value = queryId;
    }
    await load();
};
onMounted(loadByQuery);
watch(() => route.query.id, loadByQuery);
const quantity = ref(1);
const selectedSizeId = ref("");
const selectedSizeName = ref("");
const actionMessage = ref("Vui lòng chọn size và số lượng.");
const sizeWeight = (name) => {
    const map = {
        "XS": 1, "S": 2, "M": 3, "L": 4, "XL": 5, "XXL": 6, "2XL": 6, "3XL": 7, "XXXL": 7
    };
    return map[name?.toUpperCase()] || 99;
};
const sizeOptions = computed(() => {
    const product = data.value?.product;
    let opts = [];
    if (!product) {
        return [];
    }
    if (Array.isArray(product.productSizes)) {
        opts = product.productSizes.filter((x) => (x.quantity || 0) > 0).map((x) => ({
            id: x.size?.id || x.sizeId || "",
            name: x.size?.name || x.sizeName || "",
            max: x.quantity || 0
        }));
    } else if (Array.isArray(product.sizes)) {
        opts = product.sizes.map((s) => ({id: s.id || s.sizeId || "", name: s.name || s.sizeName || "", max: s.quantity || 999}));
    }
    
    return opts.sort((a, b) => {
        const wA = sizeWeight(a.name);
        const wB = sizeWeight(b.name);
        if (wA !== wB) return wA - wB;
        return String(a.name).localeCompare(String(b.name));
    });
});
const chooseSize = (option) => {
    selectedSizeId.value = option.id;
    selectedSizeName.value = option.name;
    actionMessage.value = "";
};
const decreaseQty = () => {
    if (quantity.value > 1) {
        quantity.value -= 1;
    }
};
const increaseQty = () => {
    quantity.value += 1;
};
const addToCart = async () => {
    if (!selectedSizeId.value) {
        actionMessage.value = "Vui lòng chọn size trước khi thêm vào giỏ hàng.";
        return;
    }
    try {
        await api.cart.addDetail(data.value.product.id, selectedSizeId.value, quantity.value);
        actionMessage.value = `Đã thêm vào giỏ hàng (${selectedSizeName.value}, SL ${quantity.value}).`;
    } catch (e) {
        actionMessage.value = e.message;
    }
};
const checkoutNow = async () => {
    if (!selectedSizeId.value) {
        actionMessage.value = "Vui lòng chọn size trước khi thanh toán.";
        return;
    }
    try {
        await api.cart.addDetail(data.value.product.id, selectedSizeId.value, quantity.value);
        router.push("/order/check-out");
    } catch (e) {
        actionMessage.value = e.message;
    }
};
</script>

<template>
    <main class="product-detail-page">
        <div class="container">
            <div v-if="error" class="status-message status-error">{{ error }}</div>
            
            <section class="product-detail-content" v-if="data && data.product">
                <div class="detail-image-wrap">
                    <img class="detail-image" :src="data.product.image ? '/images/' + data.product.image : '/images/product1.jpg'" :alt="data.product.name">
                </div>
                
                <div class="detail-info-wrap">
                    <h1 class="detail-product-name">{{ data.product.name }}</h1>
                    
                    <div class="price-row">
                        <span class="price-amount">{{ money(data.product.price - (data.product.price * (data.product.discount || 0) / 100)) }}</span>
                        <span class="price-currency">VNĐ</span>
                        <span class="price-old" style="text-decoration: line-through; color: #999; margin-left: 12px; font-size: 1.2rem;" v-if="data.product.discount > 0">{{ money(data.product.price) }} VNĐ</span>
                        <span class="badge" style="background: #ef4444; color: white; margin-left: 8px;" v-if="data.product.discount > 0">-{{ data.product.discount }}%</span>
                    </div>
                    
                    <div class="detail-description">
                        <p>{{ data.product.description }}</p>
                    </div>
                    
                    <div class="detail-size-wrap">
                        <div class="size-row">
                            <div class="size-title">Chọn size</div>
                            <div class="size-options-detail">
                                <button type="button" class="size-option" v-for="opt in sizeOptions" :key="opt.id" :class="{active: selectedSizeId === opt.id}" @click="chooseSize(opt)">
                                    {{ opt.name }}
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <div class="detail-stock-info" v-if="selectedSizeId" style="margin-bottom: 20px; font-size: 14px; color: #666;">
                        <span v-for="opt in sizeOptions" :key="opt.id" v-show="opt.id === selectedSizeId">
                            Còn lại: <strong>{{ opt.max || 0 }}</strong> sản phẩm
                        </span>
                    </div>
                    
                    <div class="qty-control">
                        <span class="qty-label">Số lượng</span>
                        <button type="button" class="qty-btn" @click="decreaseQty">-</button>
                        <input type="number" v-model.number="quantity" min="1">
                        <button type="button" class="qty-btn" @click="increaseQty">+</button>
                    </div>
                    
                    <div class="detail-message-wrap" v-if="actionMessage">
                        <div class="status-message" :class="{'status-error': actionMessage.includes('Vui lòng')}">
                            {{ actionMessage }}
                        </div>
                    </div>
                    
                    <div class="detail-actions">
                        <button class="btn btn-primary" type="button" @click="addToCart">Thêm vào giỏ hàng</button>
                        <button class="btn btn-outline-primary" type="button" @click="checkoutNow">Thanh toán ngay</button>
                    </div>
                </div>
            </section>
            
            <section class="product-reviews" v-if="data">
                <h2 class="section-title">Đánh giá sản phẩm</h2>
                <div class="review-card">
                    <div class="review-summary">
                        <div class="review-score-number">
                            <strong>{{ data.avgRatingValue || 0 }}</strong>
                            <span>/ 5</span>
                        </div>
                    </div>
                    <div v-if="(data.reviews || []).length" class="mt-4">
                        <div v-for="review in data.reviews" :key="review.id" class="review-display">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <strong>{{ review.account?.fullname || review.account?.username || "Khách hàng" }}</strong>
                                <span class="stars">
                                    <span v-for="s in 5" :key="s" :class="s <= (review.starRating || 0) ? 'text-warning' : 'text-muted'">★</span>
                                </span>
                            </div>
                            <p class="mb-0">{{ review.reviewContent || review.review_content || "Không có nội dung đánh giá." }}</p>
                        </div>
                    </div>
                    <div v-else class="text-muted mt-3">Chưa có đánh giá nào cho sản phẩm này.</div>
                </div>
            </section>
        </div>
    </main>
</template>
