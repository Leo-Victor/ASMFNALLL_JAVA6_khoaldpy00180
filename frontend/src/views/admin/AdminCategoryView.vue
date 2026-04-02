<script setup>
import {nextTick, ref} from "vue";
import {AdminCategoryPage} from "@/legacy/pages";
import AdminNav from "@/components/AdminNav.vue";

const {rows, modal, openEdit, save} = AdminCategoryPage.setup();
const formRef = ref(null);

const resetForm = () => {
    modal.id = "";
    modal.name = "";
    modal.editing = false;
};

const submit = async () => {
    await save();
    resetForm();
};

const editCategory = async (id) => {
    await openEdit(id);
    await nextTick();
    formRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
</script>

<template>
    <main class="container admin-product-page">
        <h3 class="page-title">Quản lý danh mục</h3>
        <div class="admin-product-shell">
            <div class="admin-product-menu">
                <AdminNav/>
            </div>
            <div class="admin-product-main">
                <form class="card admin-category-form" @submit.prevent="submit" ref="formRef">
                    <div class="form-group">
                        <label>Mã loại</label>
                        <input type="text" v-model="modal.id" :readonly="modal.editing" class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Tên loại</label>
                        <input type="text" v-model="modal.name" required class="form-control">
                    </div>
                    <div class="table-actions">
                        <button class="btn btn-primary" type="submit">{{ modal.editing ? "Cập nhật" : "Thêm" }}</button>
                        <button class="btn btn-outline-primary" type="button" @click="resetForm">{{ modal.editing ? "Huỷ" : "Làm mới" }}</button>
                    </div>
                </form>
                <div class="card admin-category-list">
                    <h4>Danh sách</h4>
                    <table>
                        <thead>
                        <tr>
                            <th>Mã loại</th>
                            <th>Tên loại</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="c in rows" :key="c.id">
                            <td>{{ c.id }}</td>
                            <td>{{ c.name }}</td>
                            <td class="table-actions">
                                <button class="btn btn-action-outline" type="button" @click="editCategory(c.id)">Sửa</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>
</template>
