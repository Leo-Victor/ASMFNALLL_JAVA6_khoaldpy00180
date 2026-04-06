<script setup>
import {AdminVipPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {rows, load, money} = AdminVipPage.setup();
const date = (value) => value ? new Date(value).toLocaleDateString("vi-VN") : "";
</script>

<template>
    <main class="container admin-product-page">
        <h3 class="page-title">Top 10 khách hàng VIP</h3>
        <div class="admin-product-shell">
            <div class="admin-product-menu">
                <AdminNav/>
            </div>
            <div class="admin-product-main">
                <div class="card">
                <table>
                    <thead>
                    <tr>
                        <th>Họ tên</th>
                        <th>Tổng mua (VND)</th>
                        <th>Mua đầu</th>
                        <th>Mua gần nhất</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="r in rows" :key="r.username">
                        <td>{{ r.fullname || r.username }}</td>
                        <td>{{ money(r.totalAmount) }}</td>
                        <td>{{ date(r.firstOrderDate || r.firstOrder) }}</td>
                        <td>{{ date(r.lastOrderDate || r.lastOrder) }}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        </div>
    </main>
</template>
