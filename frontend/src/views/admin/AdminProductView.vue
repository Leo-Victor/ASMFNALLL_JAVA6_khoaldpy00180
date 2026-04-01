<script setup>
import {computed, nextTick, ref} from "vue";
import {AdminProductPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {state, form, editing, message, load, edit, reset, save, remove, next, prev, money} = AdminProductPage.setup();
const modalOpen = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const modalMessage = ref("");
const modalMessageError = ref(false);
const filters = ref({
    keyword: "",
    categoryId: "",
    minPrice: "",
    maxPrice: ""
});
const listRef = ref(null);

const displayedProducts = computed(() => state.rows || []);

const resolveImage = (name) => name ? `/images/${name}` : "/images/logo.png";
const formatCurrency = (val) => money(val);
const openCreateModal = () => {
    reset();
    editing.value = false;
    modalMessage.value = "";
    modalMessageError.value = false;
    modalOpen.value = true;
};
const openEditModal = async (product) => {
    try {
        await edit(product.id);
    } catch (e) {
        form.id = product.id;
        form.name = product.name || "";
        form.price = product.price || "";
        form.discount = product.discount || "";
        form.quantity = product.quantity || "";
        form.available = product.available ?? true;
        form.categoryId = product.category?.id || product.categoryId || "";
        form.description = product.description || "";
        errorMessage.value = e.message || "Không tải được chi tiết sản phẩm, đang mở form với dữ liệu hiện có.";
    }
    modalMessage.value = "";
    modalMessageError.value = false;
    modalOpen.value = true;
};
const closeModal = () => {
    modalOpen.value = false;
    modalMessage.value = "";
    modalMessageError.value = false;
    reset();
};
const submitForm = async () => {
    const wasEditing = editing.value;
    try {
        await save();
        await load();
        successMessage.value = wasEditing ? "Cập nhật sản phẩm thành công" : "Thêm sản phẩm thành công";
        errorMessage.value = "";
        modalMessage.value = successMessage.value;
        modalMessageError.value = false;
        modalOpen.value = false;
    } catch (e) {
        modalMessage.value = e.message || "Cập nhật thất bại";
        modalMessageError.value = true;
        errorMessage.value = modalMessage.value;
    }
};
const removeProduct = async (product) => {
    const accepted = typeof window !== "undefined"
        ? window.confirm(`Bạn có chắc chắn muốn xoá sản phẩm "${product.name}" không?`)
        : true;
    if (!accepted) {
        return;
    }
    try {
        await remove(product.id);
        successMessage.value = "Xoá sản phẩm thành công";
        errorMessage.value = "";
    } catch (e) {
        successMessage.value = "";
        errorMessage.value = e.message || "Xoá sản phẩm thất bại";
    }
};
const scrollToResults = async () => {
    await nextTick();
    listRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
const applyFilters = async () => {
    state.page = 0;
    state.keyword = filters.value.keyword?.trim() || "";
    state.categoryId = filters.value.categoryId || "";
    state.minPrice = filters.value.minPrice === "" ? "" : Number(filters.value.minPrice);
    state.maxPrice = filters.value.maxPrice === "" ? "" : Number(filters.value.maxPrice);
    await load();
    await scrollToResults();
};
const clearFilters = async () => {
    filters.value = {keyword: "", categoryId: "", minPrice: "", maxPrice: ""};
    state.page = 0;
    state.keyword = "";
    state.categoryId = "";
    state.minPrice = "";
    state.maxPrice = "";
    await load();
    await scrollToResults();
};
</script>

<template>
    <main class="container admin-product-page">
        <h3 class="page-title">Quản lý sản phẩm</h3>
        <div class="admin-product-shell">
            <div class="admin-product-menu">
                <AdminNav/>
            </div>
            <div class="admin-product-main">
                <div class="card admin-product-list" ref="listRef">
                    <div class="admin-product-list-header">
                        <h4>Danh sách</h4>
                        <button class="btn btn-primary btn-add" type="button" @click="openCreateModal">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M8 1V15M1 8H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            Thêm sản phẩm
                        </button>
                    </div>
                    <div class="status-message status-error" v-if="errorMessage">{{ errorMessage }}</div>
                    <div class="status-message status-success" v-if="successMessage || message">{{ successMessage || message }}</div>
                    <table>
                        <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Tên</th>
                            <th>Danh mục</th>
                            <th>Giá (VND)</th>
                            <th>Số lượng</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="product in displayedProducts" :key="product.id">
                            <td class="product-image-cell"><img class="product-thumb" :src="resolveImage(product.image)" alt="Ảnh sản phẩm"></td>
                            <td>{{ product.name }}</td>
                            <td>{{ product.category?.name || product.categoryName || "-" }}</td>
                            <td>{{ formatCurrency(product.price) }}</td>
                            <td>{{ product.quantity ?? 0 }}</td>
                            <td class="table-actions">
                                <button class="btn btn-action-outline" type="button" @click="openEditModal(product)">Sửa</button>
                                <button class="btn btn-action-solid" type="button" @click="removeProduct(product)">Xoá</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="admin-product-filter">
                <form class="card admin-product-filter-form" @submit.prevent="applyFilters">
                    <div class="form-group">
                        <label>Từ khoá</label>
                        <input type="text" v-model.trim="filters.keyword" placeholder="Tên sản phẩm">
                    </div>
                    <div class="form-group">
                        <label>Thể loại</label>
                        <select v-model="filters.categoryId">
                            <option value="">Tất cả</option>
                            <option v-for="c in state.categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Giá từ</label>
                        <input type="number" step="0.01" v-model.number="filters.minPrice">
                    </div>
                    <div class="form-group">
                        <label>Giá đến</label>
                        <input type="number" step="0.01" v-model.number="filters.maxPrice">
                    </div>
                    <div class="table-actions">
                        <button class="btn btn-primary" type="submit">Áp dụng</button>
                        <button class="btn btn-outline-primary" type="button" @click="clearFilters">Xoá lọc</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="d-flex justify-content-center align-items-center gap-3 mt-3 text-center">
            <button class="btn btn-outline-primary" type="button" @click="prev">Trang trước</button>
            <div class="small text-muted fw-semibold">Trang {{ state.page + 1 }} / {{ state.totalPages || 1 }}</div>
            <button class="btn btn-outline-primary" type="button" @click="next">Trang sau</button>
        </div>
        <div class="modal-backdrop" :class="{ open: modalOpen }" @click.self="closeModal">
            <div class="admin-modal-panel">
                <div class="modal-header">
                    <h4>{{ editing ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm" }}</h4>
                    <button type="button" class="btn btn-outline-primary" @click="closeModal">Đóng</button>
                </div>
                <form class="admin-product-form" @submit.prevent="submitForm">
                    <div v-if="modalMessage" class="status-message" :class="{ 'status-error': modalMessageError }">{{ modalMessage }}</div>
                    <div class="admin-form-grid">
                        <div class="form-group">
                            <label>Tên</label>
                            <input type="text" v-model.trim="form.name" required>
                        </div>
                        <div class="form-group">
                            <label>Giá</label>
                            <input type="number" step="0.01" min="0" v-model.number="form.price" required>
                        </div>
                        <div class="form-group">
                            <label>Giảm giá (%)</label>
                            <input type="number" step="0.01" min="0" v-model.number="form.discount">
                        </div>
                        <div class="form-group">
                            <label>Số lượng</label>
                            <input type="number" min="0" v-model.number="form.quantity">
                        </div>
                        <div class="form-group full-span">
                            <label>Ảnh (tên file hoặc URL)</label>
                            <input type="text" v-model.trim="form.image">
                        </div>
                        <div class="form-group full-span">
                            <label>Mô tả</label>
                            <input type="text" v-model.trim="form.description">
                        </div>
                        <div class="form-group">
                            <label>Còn hàng</label>
                            <input type="checkbox" v-model="form.available">
                        </div>
                        <div class="form-group">
                            <label>Danh mục</label>
                            <select v-model="form.categoryId">
                                <option value="">Không chọn</option>
                                <option v-for="c in state.categories" :key="'m'+c.id" :value="c.id">{{ c.name }}</option>
                            </select>
                        </div>
                        <div class="table-actions admin-form-actions">
                            <button class="btn btn-primary" type="submit">{{ editing ? "Cập nhật" : "Thêm" }}</button>
                            <button class="btn btn-outline-primary" type="button" @click="closeModal">Huỷ</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </main>
</template>
