<script setup>
import {MyProductListPage} from "@/legacy/pages";

const {rows, error, load, money} = MyProductListPage.setup();
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Sản phẩm đã mua</h3>
        <div v-if="error" class="status-message status-error">{{ error }}</div>
        <div class="card">
            <table>
                <thead>
                <tr><th>Sản phẩm</th><th>Giá</th><th>Số lượng</th><th>Size</th><th></th></tr>
                </thead>
                <tbody>
                <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.product?.name }}</td>
                    <td>{{ money(r.price) }} VNĐ</td>
                    <td>{{ r.quantity }}</td>
                    <td>{{ r.sizeName }}</td>
                    <td><router-link class="btn btn-outline" to="/cart/index">Mua lại</router-link></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-actions" style="margin-top:10px;">
            <button class="btn" type="button" @click="load">Tải dữ liệu</button>
        </div>
    </main>
</template>
