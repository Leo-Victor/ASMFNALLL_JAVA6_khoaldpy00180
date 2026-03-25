<script setup>
import {AdminAccountPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {rows, roles, form, editing, msg, edit, reset, save, remove} = AdminAccountPage.setup();
</script>

<template>
    <main class="container admin-layout">
        <h3 class="page-title">Quản lý tài khoản</h3>
        <div class="admin-sidebar">
            <AdminNav/>
        </div>
        <div class="admin-content">
            <form class="card" @submit.prevent="save">
                <h4>Thông tin tài khoản</h4>
                <div class="admin-form-grid">
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" v-model="form.username" :readonly="editing" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Mật khẩu</label>
                        <input type="password" v-model="form.password" class="form-control" :placeholder="editing ? 'Để trống nếu không đổi' : 'Nhập mật khẩu'">
                    </div>
                    <div class="form-group">
                        <label>Họ tên</label>
                        <input type="text" v-model="form.fullname" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" v-model="form.email" required class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Vai trò</label>
                        <select v-model="form.roleId" class="form-control">
                            <option value="">Chọn vai trò</option>
                            <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.id }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Kích hoạt</label>
                        <div class="checkbox-wrapper">
                            <input type="checkbox" v-model="form.activated" id="activated">
                            <label for="activated" class="checkbox-label">Tài khoản đang hoạt động</label>
                        </div>
                    </div>
                    <div class="form-group full-span">
                        <label>Ảnh đại diện</label>
                        <input type="file" accept="image/*" class="form-control">
                        <div class="form-hint">Ảnh hiện tại: {{ form.photo || "Chưa có" }}</div>
                    </div>
                    <div class="admin-form-actions">
                        <button class="btn btn-primary" type="submit">{{ editing ? "Cập nhật" : "Thêm" }}</button>
                        <button class="btn btn-outline-primary" type="button" @click="reset">{{ editing ? "Huỷ" : "Làm mới" }}</button>
                    </div>
                </div>
            </form>
            <div class="status-message" v-if="msg">{{ msg }}</div>
            <div class="card">
                <h4>Danh sách tài khoản</h4>
                <table>
                    <thead>
                    <tr>
                        <th>Username</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="u in rows" :key="u.username">
                        <td>{{ u.username }}</td>
                        <td>{{ u.fullname }}</td>
                        <td>{{ u.email }}</td>
                        <td class="table-actions">
                            <button class="btn btn-action-outline" type="button" @click="edit(u.username)">Sửa</button>
                            <button class="btn btn-action-solid" type="button" @click="remove(u.username)">Xoá</button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</template>
