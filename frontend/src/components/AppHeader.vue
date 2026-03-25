<script setup>
import {onMounted} from "vue";
import {useRouter} from "vue-router";
import {api} from "@/api";
import {useSession} from "@/composables/useSession";

const router = useRouter();
const {state, isAuthenticated, isAdmin, refreshSession, clearSession} = useSession();
onMounted(refreshSession);

const logout = async () => {
    try {
        await api.auth.logout();
    } catch (e) {
    } finally {
        clearSession();
        await router.push("/home/index");
    }
};
</script>

<template>
    <header class="app-header">
        <nav class="navbar navbar-expand-lg">
            <div class="container">
                <router-link class="navbar-brand" to="/home/index">
                    <span class="brand-text">FASHION</span>
                    <span class="brand-accent">STORE</span>
                </router-link>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainNav">
                    <ul class="navbar-nav mx-auto mb-2 mb-lg-0">
                        <li class="nav-item"><router-link class="nav-link" to="/home/index">Trang chủ</router-link></li>
                        <li class="nav-item"><router-link class="nav-link" to="/product/list">Sản phẩm</router-link></li>
                        <li class="nav-item"><router-link class="nav-link" to="/cart/index">Giỏ hàng</router-link></li>
                        <li class="nav-item"><router-link class="nav-link" to="/order/order-list">Đơn hàng</router-link></li>
                        <li class="nav-item" v-if="isAdmin"><router-link class="nav-link" to="/admin/product">Quản trị</router-link></li>
                    </ul>
                    <div class="header-actions" v-if="!isAuthenticated">
                        <router-link class="btn btn-outline-primary btn-sm" to="/auth/login">Đăng nhập</router-link>
                        <router-link class="btn btn-primary btn-sm" to="/account/sign-up">Đăng ký</router-link>
                    </div>
                    <div class="header-user" v-else>
                        <span class="user-name">{{ state.me?.username }}</span>
                        <button class="btn btn-outline-secondary btn-sm" type="button" @click="logout">Đăng xuất</button>
                    </div>
                </div>
            </div>
        </nav>
    </header>
</template>
