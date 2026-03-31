<script setup>
import {onMounted, onUnmounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {BankTransferPage} from "@/legacy/pages";
import {api} from "@/api";

const {orderId, data, error, load, confirm: legacyConfirm, toCod, remove, money} = BankTransferPage.setup();
const route = useRoute();
const router = useRouter();
const paymentMessage = ref("");
const checkingStatus = ref(false);
const copiedKey = ref("");
const transferContent = () => String(data.value?.order?.id || orderId.value || "").trim();
const copyText = async (key, text) => {
    if (!text) {
        return;
    }
    try {
        await navigator.clipboard.writeText(String(text));
        copiedKey.value = key;
        setTimeout(() => {
            if (copiedKey.value === key) {
                copiedKey.value = "";
            }
        }, 1400);
    } catch (e) {
    }
};
let pollTimer = null;
const toOrderDetail = async () => {
    if (!orderId.value) {
        return;
    }
    paymentMessage.value = "Thanh toán thành công. Đang chuyển đến chi tiết đơn hàng...";
    setTimeout(() => {
        router.push(`/order/order-detail?id=${orderId.value}`);
    }, 600);
};
const pollPayos = async () => {
    if (!orderId.value || !data.value?.checkoutUrl) {
        return;
    }
    try {
        const res = await api.orderWorkflow.payosStatus(orderId.value);
        if (res?.data?.paid || String(res?.data?.status || "").toUpperCase() === "PAID") {
            await toOrderDetail();
        }
    } catch (e) {
    }
};
const confirm = async () => {
    checkingStatus.value = true;
    paymentMessage.value = "";
    try {
        const res = await api.orderWorkflow.confirmBankTransfer(orderId.value);
        if (res?.data?.paid) {
            await toOrderDetail();
            return;
        }
        await legacyConfirm();
        paymentMessage.value = "Hệ thống chưa ghi nhận thanh toán, vui lòng kiểm tra lại sau ít phút.";
    } catch (e) {
        paymentMessage.value = e.message || "Không kiểm tra được trạng thái thanh toán.";
    } finally {
        checkingStatus.value = false;
    }
};
onMounted(() => {
    const queryId = Number(route.query.id || route.query.orderId || "");
    if (Number.isFinite(queryId) && queryId > 0) {
        orderId.value = String(queryId);
        load();
    }
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
        <div v-if="paymentMessage" class="status-message">{{ paymentMessage }}</div>
        <div v-if="data" class="card transfer-sheet">
            <div class="transfer-sheet-top">
                Mở App Ngân hàng để <strong>quét mã QR</strong> hoặc <strong>chuyển khoản</strong> chính xác thông tin bên dưới.
            </div>
            <div class="transfer-sheet-body">
                <div class="transfer-qr-panel">
                    <div class="transfer-qr-head">QR Thanh Toán</div>
                    <img v-if="data.qrImageSrc" :src="data.qrImageSrc" alt="QR chuyển khoản" class="transfer-qr-image">
                    <div v-else class="status-message">Không thể tải mã QR, vui lòng dùng nút PayOS bên dưới.</div>
                    <div class="transfer-qr-bank">{{ data.bankName || "Ngân hàng" }}</div>
                </div>
                <div class="transfer-info-panel">
                    <div class="transfer-info-row">
                        <div class="transfer-info-label">Ngân hàng</div>
                        <div class="transfer-info-value">{{ data.bankName }}</div>
                    </div>
                    <div class="transfer-info-row">
                        <div class="transfer-info-label">Chủ tài khoản</div>
                        <div class="transfer-info-value transfer-copy-line">
                            <span>{{ data.accountName }}</span>
                            <button class="btn btn-outline transfer-copy-btn" type="button" @click="copyText('accountName', data.accountName)">{{ copiedKey === "accountName" ? "Đã chép" : "Sao chép" }}</button>
                        </div>
                    </div>
                    <div class="transfer-info-row">
                        <div class="transfer-info-label">Số tài khoản</div>
                        <div class="transfer-info-value transfer-copy-line">
                            <span>{{ data.accountNumber }}</span>
                            <button class="btn btn-outline transfer-copy-btn" type="button" @click="copyText('accountNumber', data.accountNumber)">{{ copiedKey === "accountNumber" ? "Đã chép" : "Sao chép" }}</button>
                        </div>
                    </div>
                    <div class="transfer-info-row">
                        <div class="transfer-info-label">Số tiền</div>
                        <div class="transfer-info-value transfer-copy-line">
                            <span>{{ money(data.totalPrice) }} VND</span>
                            <button class="btn btn-outline transfer-copy-btn" type="button" @click="copyText('amount', `${money(data.totalPrice)} VND`)">{{ copiedKey === "amount" ? "Đã chép" : "Sao chép" }}</button>
                        </div>
                    </div>
                    <div class="transfer-info-row">
                        <div class="transfer-info-label">Nội dung chuyển khoản</div>
                        <div class="transfer-info-value transfer-copy-line">
                            <span>{{ transferContent() }}</span>
                            <button class="btn btn-outline transfer-copy-btn" type="button" @click="copyText('content', transferContent())">{{ copiedKey === "content" ? "Đã chép" : "Sao chép" }}</button>
                        </div>
                    </div>
                    <div class="transfer-note">
                        Lưu ý: chuyển đúng số tiền <strong>{{ money(data.totalPrice) }} VND</strong> và nội dung <strong>{{ transferContent() }}</strong>.
                    </div>
                </div>
            </div>
            <div class="transfer-actions">
                <a class="btn" v-if="data.checkoutUrl" :href="data.checkoutUrl" target="_blank" rel="noopener">Thanh toán qua PayOS</a>
                <button class="btn btn-outline" type="button" :disabled="checkingStatus" @click="confirm">{{ checkingStatus ? "Đang kiểm tra..." : "Kiểm tra trạng thái" }}</button>
                <button class="btn btn-outline" type="button" @click="toCod">Chuyển sang COD</button>
                <button class="btn btn-outline" type="button" @click="remove">Hủy thanh toán</button>
            </div>
        </div>
    </main>
</template>
