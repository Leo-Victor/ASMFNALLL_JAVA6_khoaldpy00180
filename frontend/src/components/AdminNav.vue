<script setup>
import {computed} from "vue";
import {useRoute} from "vue-router";

const route = useRoute();
const isRevenueRoute = computed(() => route.path.startsWith("/admin/revenue"));
</script>

<template>
    <nav class="admin-nav">
        <router-link class="admin-nav-link" to="/admin/category">
            <span>Danh mục</span>
        </router-link>
        <router-link class="admin-nav-link" to="/admin/product">
            <span>Sản phẩm</span>
        </router-link>
        <router-link class="admin-nav-link" to="/admin/account">
            <span>Tài khoản</span>
        </router-link>
        <router-link class="admin-nav-link" to="/admin/order">
            <span>Đơn hàng</span>
        </router-link>
        <router-link class="admin-nav-link" to="/admin/camera">
            <span>Camera</span>
        </router-link>
        <router-link class="admin-nav-link admin-nav-parent" :class="{'is-open': isRevenueRoute}" to="/admin/revenue">
            <span>Doanh thu</span>
            <span class="admin-nav-chevron">{{ isRevenueRoute ? "▾" : "▸" }}</span>
        </router-link>
        <div v-if="isRevenueRoute" class="admin-nav-submenu">
            <router-link class="admin-nav-sublink" to="/admin/revenue/day">Ngày</router-link>
            <router-link class="admin-nav-sublink" to="/admin/revenue/week">Tuần</router-link>
            <router-link class="admin-nav-sublink" to="/admin/revenue/month">Tháng</router-link>
            <router-link class="admin-nav-sublink" to="/admin/revenue/quarter">Quý</router-link>
            <router-link class="admin-nav-sublink" to="/admin/revenue/year">Năm</router-link>
        </div>
        <router-link class="admin-nav-link" to="/admin/vip">
            <span>Khách VIP</span>
        </router-link>
    </nav>
</template>
