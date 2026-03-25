<script setup>
import {nextTick, ref} from "vue";
import {AdminRevenuePage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {params, rows, total, load, money} = AdminRevenuePage.setup();
const revenueTableRef = ref(null);
const scrollToRevenueTable = async () => {
    await nextTick();
    revenueTableRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
const applyFilters = async () => {
    await load();
    await scrollToRevenueTable();
};
const clearFilters = async () => {
    params.fromDate = "";
    params.toDate = "";
    params.sortField = "orderId";
    params.sortDir = "asc";
    await load();
    await scrollToRevenueTable();
};
</script>

<template>
    <main class="container admin-layout">
        <h3 class="page-title">Doanh thu theo đơn hàng</h3>
        <div class="admin-sidebar">
            <AdminNav/>
        </div>
        <div class="admin-content">
            <div class="revenue-title" style="background: #1a1a1a !important; color: #ffffff !important; padding: 20px !important;">BẢNG DOANH THU BÁN HÀNG</div>
            <form class="card revenue-filter" @submit.prevent="applyFilters">
                <div class="form-group">
                    <label>Từ ngày</label>
                    <input type="date" v-model="params.fromDate" class="form-control">
                </div>
                <div class="form-group">
                    <label>Đến ngày</label>
                    <input type="date" v-model="params.toDate" class="form-control">
                </div>
                <div class="form-group">
                    <label>Sắp xếp theo</label>
                    <select v-model="params.sortField" class="form-control">
                        <option value="orderId">Mã đơn hàng</option>
                        <option value="productName">Tên sản phẩm</option>
                        <option value="quantity">Số lượng</option>
                        <option value="unitPrice">Đơn giá</option>
                        <option value="discountAmount">Giảm giá</option>
                        <option value="lineTotal">Thành tiền</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Chiều sắp xếp</label>
                    <select v-model="params.sortDir" class="form-control">
                        <option value="asc">Tăng dần</option>
                        <option value="desc">Giảm dần</option>
                    </select>
                </div>
                <div class="table-actions">
                    <button class="btn btn-primary" type="submit">Lọc</button>
                    <button class="btn btn-outline-primary" type="button" @click="clearFilters">Xoá lọc</button>
                </div>
            </form>
            <div class="card revenue-table-wrap" ref="revenueTableRef">
                <table class="revenue-table revenue-table-head">
                    <colgroup>
                        <col style="width: 6%;">
                        <col style="width: 15%;">
                        <col style="width: 29%;">
                        <col style="width: 10%;">
                        <col style="width: 13%;">
                        <col style="width: 12%;">
                        <col style="width: 15%;">
                    </colgroup>
                    <thead>
                    <tr>
                        <th>STT</th>
                        <th>Mã đơn hàng</th>
                        <th>Tên sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Giảm giá</th>
                        <th>Thành tiền</th>
                    </tr>
                    </thead>
                </table>
                <div class="revenue-table-scroll">
                    <table class="revenue-table revenue-table-body">
                        <colgroup>
                            <col style="width: 6%;">
                            <col style="width: 15%;">
                            <col style="width: 29%;">
                            <col style="width: 10%;">
                            <col style="width: 13%;">
                            <col style="width: 12%;">
                            <col style="width: 15%;">
                        </colgroup>
                    <tbody>
                    <tr v-for="(r, i) in rows" :key="r.orderId + '-' + r.productId">
                        <td class="cell-center">{{ i + 1 }}</td>
                        <td class="cell-center">#{{ r.orderId }}</td>
                        <td>{{ r.productName }}</td>
                        <td class="cell-center">{{ r.quantity }}</td>
                        <td class="cell-center">{{ money(r.unitPrice) }}</td>
                        <td class="cell-center">{{ money(r.discountAmount) }}</td>
                        <td class="cell-center">{{ money(r.lineTotal) }}</td>
                    </tr>
                    </tbody>
                    </table>
                </div>
                <table class="revenue-table revenue-table-foot">
                    <colgroup>
                        <col style="width: 6%;">
                        <col style="width: 15%;">
                        <col style="width: 29%;">
                        <col style="width: 10%;">
                        <col style="width: 13%;">
                        <col style="width: 12%;">
                        <col style="width: 15%;">
                    </colgroup>
                    <tfoot>
                    <tr class="revenue-total-row">
                        <td colspan="6" class="total-label">Tổng cộng</td>
                        <td class="total-value">{{ money(total) }}</td>
                    </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </main>
</template>
