<script setup>
import {ChangePasswordPage} from "@/legacy/pages";

const {form, message, submit} = ChangePasswordPage.setup();
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Đổi mật khẩu</h3>
        <div v-if="message" class="status-message status-error">{{ message }}</div>
        <form class="card" @submit.prevent="submit">
            <div class="form-group">
                <label>Mật khẩu hiện tại</label>
                <input type="password" v-model="form.currentPassword" required>
            </div>
            <div class="form-group">
                <label>Mật khẩu mới</label>
                <input type="password" v-model="form.newPassword" required>
            </div>
            <button class="btn" type="submit">Đổi mật khẩu</button>
        </form>
    </main>
</template>
