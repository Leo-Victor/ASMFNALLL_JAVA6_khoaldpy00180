<script setup>
import {onMounted, onUnmounted} from "vue";
import {BankTransferPage} from "@/legacy/pages";
import {api} from "@/api";

const {orderId, data, error, load, confirm, toCod, remove, money} = BankTransferPage.setup();
let pollTimer = null;
const pollPayos = async () => {
    if (!orderId.value || !data.value?.checkoutUrl) {
        return;
    }
    try {
        const res = await api.orderWorkflow.payosStatus(orderId.value);
        const redirectUrl = res?.data?.redirectUrl;
        if (redirectUrl) {
            window.location.href = redirectUrl;
        }
    } catch (e) {
    }
};
onMounted(() => {
    pollTimer = setInterval(pollPayos, 5000);
});
onUnmounted(() => {
    if (pollTimer) {
        clearInterval(pollTimer);
    }
});
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Thanh toán chuyển khoản ngân hàng</h3>
        <div class="row g-2 mb-3">
            <div class="col-md-3"><input v-model="orderId" class="form-control" placeholder="orderId"></div>
            <div class="col-md-2"><button class="btn btn-primary w-100" @click="load">Tải</button></div>
        </div>
        <div v-if="error" class="status-message status-error">{{ error }}</div>
        <div v-if="data" class="card bank-transfer-block">
            <div class="card bank-transfer-total">
                <div class="bank-transfer-total-label">Số tiền thanh toán:</div>
                <div class="bank-transfer-total-value">{{ money(data.totalPrice) }}đ</div>
            </div>
            <div class="bank-transfer-qr">
                <div class="bank-transfer-qr-title">Quét mã QR để chuyển khoản</div>
                <img v-if="data.qrImageSrc" :src="data.qrImageSrc" alt="QR chuyển khoản" class="bank-transfer-qr-image">
                <div v-else class="status-message">Không thể tải mã QR, vui lòng dùng nút thanh toán PayOS bên dưới.</div>
            </div>
            <details class="card bank-transfer-detail-card">
                <summary class="bank-transfer-detail-title">Chi tiết thông tin chuyển khoản</summary>
                <div class="bank-transfer-detail-body">
                    <div><strong>Ngân hàng:</strong> <span>{{ data.bankName }}</span></div>
                    <div><strong>Số tài khoản:</strong> <span>{{ data.accountNumber }}</span></div>
                    <div><strong>Thụ hưởng:</strong> <span>{{ data.accountName }}</span></div>
                    <div><strong>Nội dung CK:</strong> <span>{{ data.order?.id }}</span></div>
                    <div><strong>Số tiền:</strong> <span>{{ money(data.totalPrice) }} VNĐ</span></div>
                </div>
            </details>
            <div class="bank-transfer-actions">
                <a class="btn" v-if="data.checkoutUrl" :href="data.checkoutUrl" target="_blank" rel="noopener">Thanh toán qua PayOS</a>
                <button class="btn btn-outline" type="button" @click="confirm">Kiểm tra trạng thái</button>
                <button class="btn btn-outline" type="button" @click="toCod">Chuyển sang COD</button>
                <button class="btn btn-outline" type="button" @click="remove">Hủy thanh toán</button>
            </div>
        </div>
    </main>
</template>
