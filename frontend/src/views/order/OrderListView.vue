<script setup>
import {OrderListPage} from "@/legacy/pages";

const {orders, error, load, dateTime} = OrderListPage.setup();
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
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Đơn hàng của tôi</h3>
        <div v-if="error" class="alert alert-danger">{{ error }}</div>
        <div class="card">
            <table>
                <thead><tr><th>Mã đơn</th><th>Ngày</th><th>Trạng thái</th><th>Địa chỉ</th><th></th></tr></thead>
                <tbody>
                <tr v-for="o in orders" :key="o.id">
                    <td>{{ o.id }}</td>
                    <td>{{ dateTime(o.createDate) }}</td>
                    <td><span class="badge" style="color:black;">{{ statusLabel(o.status) }}</span></td>
                    <td>{{ o.address }}</td>
                    <td class="table-actions">
                        <router-link class="btn btn-outline" :to="'/order/order-detail?id=' + o.id">Xem chi tiết</router-link>
                        <router-link class="btn btn-outline" to="/order/my-product-list">Mua lại</router-link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-actions" style="margin-top:10px;">
            <button class="btn" type="button" @click="load">Tải danh sách</button>
        </div>
    </main>
</template>
