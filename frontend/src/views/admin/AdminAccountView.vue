<script setup>
import {computed, ref} from "vue";
import {AdminAccountPage} from "@/legacy/pages";
import {useSession} from "@/composables/useSession";
import AdminNav from "@/components/AdminNav.vue";
import {api} from "@/api";

const {rows, roles, form, modalOpen, editing, msg, load, openCreate, closeModal, onPhotoChange, save} = AdminAccountPage.setup();
const {state} = useSession();
const currentUsername = computed(() => state.me?.username || "");
const visibleRows = computed(() => (rows.value || []).filter((u) => u?.username !== currentUsername.value));
const previewOpen = ref(false);
const previewSrc = ref("");
const previewName = ref("");
const previewImageError = ref(false);
const updateNoticeOpen = ref(false);
const updateNoticeText = ref("");

const roleOptions = computed(() => (roles.value || []).map((r) => r.id));
const roleLabel = (roleId) => {
    const key = String(roleId || "").toUpperCase();
    if (key === "ADMIN") return "Quản trị viên";
    if (key === "USER") return "Khách hàng";
    return key || "Khách hàng";
};
const openPreview = (u) => {
    const raw = String(u?.photo || "").trim();
    previewName.value = u?.username || "";
    previewSrc.value = raw
        ? (/^https?:\/\//i.test(raw) ? raw : (raw.startsWith("/") ? raw : `/images/${raw}`))
        : "/images/logo.png";
    previewImageError.value = false;
    previewOpen.value = true;
};
const closePreview = () => {
    previewOpen.value = false;
    previewSrc.value = "";
    previewName.value = "";
    previewImageError.value = false;
};
const previewInitial = computed(() => {
    const text = String(previewName.value || "").trim();
    return text ? text.slice(0, 1).toUpperCase() : "U";
});
const openUpdateNotice = (text) => {
    updateNoticeText.value = text || "Cập nhật thành công.";
    updateNoticeOpen.value = true;
};
const closeUpdateNotice = () => {
    updateNoticeOpen.value = false;
    updateNoticeText.value = "";
};
const updateRole = async (u, event) => {
    const nextRole = String(event?.target?.value || "").trim();
    if (!u?.username || !nextRole) return;
    try {
        await api.admin.accounts.updateRole(u.username, nextRole);
        await load();
        openUpdateNotice(`Đã cập nhật vai trò của tài khoản ${u.username}.`);
    } catch (e) {
        event.target.value = u.roleId || "USER";
    }
};
const updateActivation = async (u, event) => {
    if (!u?.username) return;
    const value = String(event?.target?.value || "active");
    const activated = value === "active";
    try {
        await api.admin.accounts.updateActivation(u.username, activated);
        await load();
        openUpdateNotice(`Đã cập nhật trạng thái tài khoản ${u.username}.`);
    } catch (e) {
        event.target.value = u.activated ? "active" : "locked";
    }
};
</script>

<template>
    <main class="container admin-product-page">
        <h3 class="page-title">Quản lý tài khoản</h3>
        <div class="admin-product-shell">
            <div class="admin-product-menu">
                <AdminNav/>
            </div>
            <div class="admin-product-main">
            <div class="status-message" v-if="msg">{{ msg }}</div>
            <div class="card admin-product-list">
                <div class="admin-product-list-header">
                    <h4>Danh sách tài khoản</h4>
                    <button class="btn btn-primary btn-add" type="button" @click="openCreate">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M8 1V15M1 8H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        Thêm tài khoản
                    </button>
                </div>
                <div style="overflow-x: auto; width: 100%;">
                    <table style="min-width: 1000px; width: 100%; white-space: nowrap;">
                        <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Username</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Địa chỉ</th>
                            <th>Loại tài khoản</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="u in visibleRows" :key="u.username">
                            <td>
                                <button class="btn btn-action-outline" type="button" @click="openPreview(u)">Xem ảnh</button>
                            </td>
                            <td>{{ u.username }}</td>
                            <td>{{ u.fullname }}</td>
                            <td>{{ u.email }}</td>
                            <td>{{ u.phone }}</td>
                            <td style="white-space: normal; min-width: 200px;">{{ u.address }}</td>
                            <td>{{ u.accountType === "GOOGLE" ? "Google" : "Bình thường" }}</td>
                            <td>
                                <select :value="u.roleId || 'USER'" @change="updateRole(u, $event)">
                                    <option v-for="r in roleOptions" :key="u.username + '_' + r" :value="r">{{ roleLabel(r) }}</option>
                                </select>
                            </td>
                            <td>
                                <select :value="u.activated ? 'active' : 'locked'" @change="updateActivation(u, $event)">
                                    <option value="active">Mở khoá</option>
                                    <option value="locked">Khoá</option>
                                </select>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        </div>
        <div class="modal-backdrop" :class="{open: modalOpen}" v-if="modalOpen" @click.self="closeModal">
            <div class="admin-modal-panel">
                <form @submit.prevent="save">
                    <div class="modal-header">
                        <h4>{{ editing ? "Cập nhật tài khoản" : "Thêm tài khoản" }}</h4>
                        <button type="button" class="btn btn-outline-primary" @click="closeModal">Đóng</button>
                    </div>
                    <div class="admin-form-grid">
                        <div class="form-group">
                            <label>Username</label>
                            <input type="text" v-model="form.username" :readonly="editing" required class="form-control">
                        </div>
                        <div class="form-group" v-if="!editing">
                            <label>Mật khẩu</label>
                            <input type="password" v-model="form.password" class="form-control" placeholder="Nhập mật khẩu" required>
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
                            <label>Số điện thoại</label>
                            <input type="text" v-model.trim="form.phone" required class="form-control" pattern="^(0|\+84)\d{9,10}$" placeholder="VD: 0912345678">
                        </div>
                        <div class="form-group">
                            <label>Địa chỉ</label>
                            <input type="text" v-model.trim="form.address" required class="form-control" placeholder="Nhập địa chỉ">
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
                            <input type="file" accept="image/*" class="form-control" @change="onPhotoChange">
                            <div class="form-hint">Ảnh hiện tại: {{ form.photo || "Chưa có" }}</div>
                        </div>
                        <div class="admin-form-actions">
                            <button class="btn btn-primary" type="submit">{{ editing ? "Cập nhật" : "Thêm" }}</button>
                            <button class="btn btn-outline-primary" type="button" @click="closeModal">Huỷ</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-backdrop" :class="{open: previewOpen}" v-if="previewOpen" @click.self="closePreview">
            <div class="admin-modal-panel" style="max-width: 360px; text-align: center;">
                <h4 style="margin: 0 0 12px 0;">Ảnh đại diện: {{ previewName }}</h4>
                <img
                    v-if="!previewImageError"
                    :src="previewSrc"
                    alt="avatar"
                    style="width:300px;height:300px;object-fit:cover;border-radius:12px;border:1px solid #e5e7eb;"
                    @error="previewImageError = true"
                >
                <div
                    v-else
                    style="width:300px;height:300px;border-radius:12px;border:1px solid #e5e7eb;background:#eef2ff;color:#3730a3;font-size:120px;font-weight:800;display:flex;align-items:center;justify-content:center;user-select:none;"
                >{{ previewInitial }}</div>
                <div style="margin-top: 12px;">
                    <button class="btn btn-outline-primary" type="button" @click="closePreview">Đóng</button>
                </div>
            </div>
        </div>
        <div class="modal-backdrop" :class="{open: updateNoticeOpen}" v-if="updateNoticeOpen" @click.self="closeUpdateNotice">
            <div class="admin-modal-panel" style="max-width: 420px; text-align: center;">
                <h4 style="margin: 0 0 8px 0;">Thông báo</h4>
                <p style="margin: 0; color: #374151;">{{ updateNoticeText }}</p>
                <div style="margin-top: 14px;">
                    <button class="btn btn-primary" type="button" @click="closeUpdateNotice">Đã hiểu</button>
                </div>
            </div>
        </div>
    </main>
</template>
