<script setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {OrderDetailPage} from "@/legacy/pages";
import {api} from "@/api";

const {orderId, data, payos, error, load, checkPayos, money} = OrderDetailPage.setup();
const route = useRoute();
const reviewForms = reactive({});
const reviewMessage = ref("");
const reviewedSet = computed(() => new Set(data.value?.reviewedProductIds || []));
const isReviewable = computed(() => !!data.value?.reviewable);
const reviewKey = (detail) => String(detail.product?.id || detail.productId || "");
const ensureReviewForm = (detail) => {
    const key = reviewKey(detail);
    if (!reviewForms[key]) {
        reviewForms[key] = {starRating: 5, reviewContent: "", images: []};
    }
    return reviewForms[key];
};
const onReviewImagesChange = (detail, event) => {
    const form = ensureReviewForm(detail);
    form.images = Array.from(event.target.files || []);
};
const submitReview = async (detail) => {
    const productId = detail.product?.id || detail.productId;
    if (!productId || !data.value?.order?.id) {
        reviewMessage.value = "Không xác định được sản phẩm hoặc đơn hàng.";
        return;
    }
    const form = ensureReviewForm(detail);
    try {
        const response = await api.reviews.createFromOrder({
            orderId: data.value.order.id,
            productId,
            starRating: Number(form.starRating || 5),
            reviewContent: form.reviewContent || "",
            images: form.images || []
        });
        reviewMessage.value = response.message || "Đã gửi đánh giá.";
        await load();
    } catch (e) {
        reviewMessage.value = e.message;
    }
};
const statusLabel = (status) => {
    const map = {
        PLACED_PAID: "Đã đặt - đã TT",
        PLACED_UNPAID: "Đã đặt - chưa TT",
        NEW: "Đã đặt - chưa TT",
        PLACED: "Đã đặt - chưa TT",
        SHIPPING_PAID: "Đang giao - đã TT",
        SHIPPING_UNPAID: "Đang giao - chưa TT",
        SHIPPING: "Đang giao - chưa TT",
        DONE: "Giao thành công",
        DELIVERED_SUCCESS: "Giao thành công",
        CANCEL: "Giao thất bại",
        DELIVERY_FAILED: "Giao thất bại"
    };
    return map[status] || status;
};
const initOrderByQuery = async () => {
    const queryId = Number(route.query.id || route.query.orderId || "");
    if (!Number.isFinite(queryId) || queryId <= 0) {
        return;
    }
    orderId.value = String(queryId);
    await load();
};
onMounted(initOrderByQuery);
watch(() => route.query.id, initOrderByQuery);
watch(() => route.query.orderId, initOrderByQuery);
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Chi tiết đơn hàng</h3>
        <div class="row g-2 mb-3">
            <div class="col-md-3"><input v-model="orderId" class="form-control" placeholder="orderId"></div>
            <div class="col-md-2"><button class="btn btn-primary w-100" @click="load">Tải</button></div>
            <div class="col-md-2"><button class="btn btn-outline-secondary w-100" @click="checkPayos">Kiểm tra PayOS</button></div>
        </div>
        <div v-if="error" class="status-message">{{ error }}</div>
        <div class="card order-detail-summary" v-if="data">
            <div>
                Mã đơn: <strong>{{ data.order?.id }}</strong>
                - Ngày: <span>{{ data.order?.createDate }}</span>
                - Trạng thái: <span class="badge">{{ statusLabel(data.order?.status) }}</span>
            </div>
            <div>Địa chỉ: <span>{{ data.order?.address }}</span></div>
        </div>
        <div v-if="reviewMessage" class="status-message">{{ reviewMessage }}</div>
        <h4 class="page-title">Sản phẩm</h4>
        <div class="card" v-if="data">
            <table>
                <thead>
                <tr><th>Sản phẩm</th><th>Giá</th><th>Số lượng</th><th>Size</th><th></th></tr>
                </thead>
                <tbody>
                <template v-for="d in (data.details||[])" :key="d.id">
                    <tr>
                        <td><router-link :to="'/product/detail?id=' + (d.product?.id || '')">{{ d.product?.name || d.productName }}</router-link></td>
                        <td>{{ money(d.price) }} VNĐ</td>
                        <td>{{ d.quantity }}</td>
                        <td>{{ d.sizeName }}</td>
                        <td><router-link class="btn btn-outline" :to="'/cart/index'">Mua lại</router-link></td>
                    </tr>
                    <tr>
                        <td colspan="5">
                            <form v-if="isReviewable && !reviewedSet.has(d.product?.id)" class="card" @submit.prevent="submitReview(d)">
                                <div class="form-group">
                                    <label>Đánh giá sao</label>
                                    <select v-model="ensureReviewForm(d).starRating">
                                        <option :value="5">5 sao</option>
                                        <option :value="4">4 sao</option>
                                        <option :value="3">3 sao</option>
                                        <option :value="2">2 sao</option>
                                        <option :value="1">1 sao</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>Nội dung đánh giá</label>
                                    <textarea v-model="ensureReviewForm(d).reviewContent" rows="3" placeholder="Chia sẻ trải nghiệm của bạn"></textarea>
                                </div>
                                <div class="form-group">
                                    <label>Ảnh đính kèm</label>
                                    <input type="file" multiple accept="image/*" @change="onReviewImagesChange(d, $event)">
                                </div>
                                <div class="table-actions">
                                    <button class="btn" type="submit">Gửi đánh giá</button>
                                </div>
                            </form>
                            <div v-else-if="reviewedSet.has(d.product?.id)" class="status-message">Bạn đã đánh giá sản phẩm này.</div>
                            <div v-else class="status-message">Chỉ được đánh giá khi đơn hàng giao thành công.</div>
                        </td>
                    </tr>
                </template>
                </tbody>
            </table>
        </div>
        <div v-if="payos" class="status-message">{{ payos.status || payos.message }}</div>
    </main>
</template>
