<script setup>
import {AdminVipPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";
import {computed, reactive, ref} from "vue";

const {rows, load, money} = AdminVipPage.setup();
const date = (value) => value ? new Date(value).toLocaleDateString("vi-VN") : "";
const imageModalOpen = ref(false);
const previewImage = ref("");
const exporting = ref(false);
const columnVisible = reactive({
    photo: true,
    email: true,
    address: true,
    phone: true,
    totalAmount: true,
    firstOrder: true,
    lastOrder: true
});
const togglableColumns = [
    {key: "photo", label: "Ảnh"},
    {key: "email", label: "Email"},
    {key: "address", label: "Địa chỉ"},
    {key: "phone", label: "Số điện thoại"},
    {key: "totalAmount", label: "Tổng mua"},
    {key: "firstOrder", label: "Mua lần đầu"},
    {key: "lastOrder", label: "Lần mua gần nhất"}
];
const showColumn = (key) => Boolean(columnVisible[key]);
const openImage = (value) => {
    const resolved = resolvePhotoUrl(value);
    if (!resolved) {
        return;
    }
    previewImage.value = resolved;
    imageModalOpen.value = true;
};
const closeImage = () => {
    imageModalOpen.value = false;
    previewImage.value = "";
};
const exportVip = async () => {
    if (exporting.value) {
        return;
    }
    exporting.value = true;
    try {
        const res = await fetch("/api/admin/reports/vip/export?format=xlsx", {credentials: "include"});
        if (!res.ok) {
            throw new Error("Không thể xuất file VIP");
        }
        const blob = await res.blob();
        const a = document.createElement("a");
        const u = window.URL.createObjectURL(blob);
        a.href = u;
        const cd = res.headers.get("content-disposition") || "";
        const match = cd.match(/filename=\"?([^\";]+)\"?/i);
        a.download = match?.[1] || "khach-vip.xlsx";
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(u);
    } finally {
        exporting.value = false;
    }
};
const resolvePhotoUrl = (photo) => {
    const raw = String(photo || "").trim();
    if (!raw) {
        return "";
    }
    if (/^https?:\/\//i.test(raw)) {
        return raw;
    }
    if (raw.startsWith("/")) {
        return raw;
    }
    const backendUrl = String(import.meta.env.VITE_BACKEND_URL || "").trim().replace(/\/$/, "");
    if (backendUrl) {
        return `${backendUrl}/images/${encodeURIComponent(raw)}`;
    }
    return `/images/${encodeURIComponent(raw)}`;
};
const tableColspan = computed(() => {
    let total = 2;
    for (const item of togglableColumns) {
        if (showColumn(item.key)) {
            total += 1;
        }
    }
    return total;
});
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
                    <div class="vip-toolbar">
                        <details class="vip-column-dropdown">
                            <summary class="vip-column-dropdown-trigger">Ẩn hiện cột</summary>
                            <div class="vip-column-dropdown-menu">
                                <label v-for="c in togglableColumns" :key="c.key" class="vip-column-option">
                                    <input type="checkbox" v-model="columnVisible[c.key]">
                                    <span>{{ c.label }}</span>
                                </label>
                            </div>
                        </details>
                        <button class="btn btn-outline-primary" type="button" @click="load">Tải lại</button>
                        <button class="btn btn-primary" type="button" @click="exportVip" :disabled="exporting">{{ exporting ? "Đang xuất..." : "Xuất Excel" }}</button>
                    </div>
                    <div class="vip-table-wrap">
                        <table class="vip-table">
                            <thead>
                            <tr>
                                <th class="sticky-col sticky-col-1">Username</th>
                                <th class="sticky-col sticky-col-2">Họ tên</th>
                                <th v-if="showColumn('photo')">Ảnh</th>
                                <th v-if="showColumn('email')">Email</th>
                                <th v-if="showColumn('address')">Địa chỉ</th>
                                <th v-if="showColumn('phone')">Số điện thoại</th>
                                <th v-if="showColumn('totalAmount')">Tổng mua (VND)</th>
                                <th v-if="showColumn('firstOrder')">Mua lần đầu</th>
                                <th v-if="showColumn('lastOrder')">Lần mua gần nhất</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="r in rows" :key="r.username">
                                <td class="sticky-col sticky-col-1">{{ r.username }}</td>
                                <td class="sticky-col sticky-col-2">{{ r.fullname || r.username }}</td>
                                <td v-if="showColumn('photo')">
                                    <button class="btn btn-action-outline" type="button" @click="openImage(r.photo)" :disabled="!r.photo">Xem ảnh</button>
                                </td>
                                <td v-if="showColumn('email')">{{ r.email || "" }}</td>
                                <td v-if="showColumn('address')">
                                    <div class="vip-address-scroll">{{ r.address || "" }}</div>
                                </td>
                                <td v-if="showColumn('phone')">{{ r.phone || "" }}</td>
                                <td v-if="showColumn('totalAmount')">{{ money(r.totalAmount) }}</td>
                                <td v-if="showColumn('firstOrder')">{{ date(r.firstOrderDate || r.firstOrder) }}</td>
                                <td v-if="showColumn('lastOrder')">{{ date(r.lastOrderDate || r.lastOrder) }}</td>
                            </tr>
                            <tr v-if="!rows.length">
                                <td :colspan="tableColspan" class="order-empty-row">Không có dữ liệu VIP.</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="imageModalOpen" class="modal-overlay" @click.self="closeImage">
            <div class="modal-content" style="max-width: 360px;">
                <div class="modal-header">
                    <h5>Xem ảnh khách hàng</h5>
                    <button class="modal-close" type="button" @click="closeImage">×</button>
                </div>
                <div class="modal-body" style="display:flex;justify-content:center;">
                    <img :src="previewImage" alt="Ảnh khách hàng" style="width:300px;height:300px;object-fit:cover;border-radius:12px;border:1px solid #e5e7eb;">
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.vip-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    margin-bottom: 10px;
}

.vip-column-filter {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 10px;
    margin-right: auto;
}

.vip-column-filter-title {
    font-weight: 700;
    color: #374151;
}

.vip-column-dropdown {
    position: relative;
    margin-right: auto;
}

.vip-column-dropdown-trigger {
    list-style: none;
    border: 1px solid #d1d5db;
    border-radius: 10px;
    background: #fff;
    color: #111827;
    font-weight: 600;
    padding: 8px 12px;
    cursor: pointer;
}

.vip-column-dropdown-trigger::-webkit-details-marker {
    display: none;
}

.vip-column-dropdown[open] .vip-column-dropdown-trigger {
    border-color: #111827;
}

.vip-column-dropdown-menu {
    position: absolute;
    top: calc(100% + 6px);
    left: 0;
    min-width: 220px;
    background: #fff;
    border: 1px solid #d1d5db;
    border-radius: 10px;
    box-shadow: 0 12px 26px rgba(15, 23, 42, 0.12);
    padding: 10px;
    display: grid;
    gap: 8px;
    z-index: 10;
}

.vip-column-option {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
}

.vip-table-wrap {
    overflow-x: auto;
}

.vip-table {
    min-width: 1300px;
}

.sticky-col {
    position: sticky;
    z-index: 2;
    background: #fff;
}

.sticky-col-1 {
    left: 0;
    min-width: 150px;
}

.sticky-col-2 {
    left: 150px;
    min-width: 190px;
}

.vip-table thead .sticky-col {
    z-index: 3;
    background: #f9fafb;
}

.vip-address-scroll {
    max-width: 320px;
    overflow-x: auto;
    white-space: nowrap;
}

.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1500;
}

.modal-content {
    width: min(92vw, 360px);
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 14px;
    border-bottom: 1px solid #e5e7eb;
}

.modal-header h5 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
}

.modal-close {
    border: 1px solid #d1d5db;
    background: #fff;
    width: 28px;
    height: 28px;
    line-height: 1;
    border-radius: 6px;
}

.modal-body {
    padding: 14px;
}
</style>
