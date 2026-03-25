<script setup>
import {EditProfilePage} from "@/legacy/pages";

const {form, message, save} = EditProfilePage.setup();
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Cập nhật thông tin</h3>
        <div v-if="message" class="status-message">{{ message }}</div>
        <form class="card" @submit.prevent="save">
            <div class="form-group">
                <label>Họ tên</label>
                <input type="text" v-model="form.fullname" required>
            </div>
            <div class="form-group">
                <label>Email</label>
                <input type="email" v-model="form.email" required>
            </div>
            <div class="form-group">
                <label>Ảnh đại diện</label>
                <input type="file" accept="image/*">
                <div class="form-note">Ảnh hiện tại: <span>{{ form.photo || "Chưa có" }}</span></div>
            </div>
            <button class="btn" type="submit">Cập nhật</button>
        </form>
    </main>
</template>
