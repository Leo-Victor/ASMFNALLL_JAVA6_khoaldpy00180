<script setup>
import {ProductListPage} from "@/legacy/pages";
import {nextTick, ref} from "vue";

const {filter, data, loading, error, load, next, prev, productCard} = ProductListPage.setup();
const money = (value) => Number(value || 0).toLocaleString("vi-VN");
const discountPercent = (product) => Math.max(0, Number(product?.discount || 0));
const hasDiscount = (product) => discountPercent(product) > 0;
const finalPrice = (product) => {
    const price = Number(product?.price || 0);
    const percent = discountPercent(product);
    return Math.max(0, price - (price * percent / 100));
};
const resultsRef = ref(null);
const scrollToResults = async () => {
    await nextTick();
    resultsRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
const applySearchFilters = async () => {
    filter.page = 0;
    await load();
    await scrollToResults();
};
const applySelectFilters = async () => {
    filter.page = 0;
    await load();
    await scrollToResults();
};
const clearFilters = async () => {
    filter.keyword = "";
    filter.categoryId = "";
    filter.page = 0;
    await load();
    await scrollToResults();
};
</script>

<template>
    <main class="product-list-page">
        <div class="container">
            <div class="catalog-layout">
                <aside class="filters-sidebar">
                    <form class="filters-vertical card" @submit.prevent="applySearchFilters">
                        <div class="filter-group">
                            <h4 class="filter-title">Tìm kiếm</h4>
                            <div class="form-group">
                                <input type="text" v-model="filter.keyword" class="form-control" placeholder="Tên sản phẩm...">
                            </div>
                        </div>
                        <div class="filter-group">
                            <h4 class="filter-title">Danh mục</h4>
                            <div class="form-group">
                                <select v-model="filter.categoryId" class="form-control" @change="applySelectFilters">
                                    <option value="">Tất cả danh mục</option>
                                    <option v-for="c in (data.categories || [])" :key="c.id" :value="c.id">{{ c.name }}</option>
                                </select>
                            </div>
                        </div>
                        <div class="filter-group">
                            <h4 class="filter-title">Sắp xếp</h4>
                            <div class="form-group">
                                <select v-model="filter.sort" class="form-control" @change="applySelectFilters">
                                    <option value="">Mặc định</option>
                                    <option value="priceAsc">Giá tăng dần</option>
                                    <option value="priceDesc">Giá giảm dần</option>
                                </select>
                            </div>
                        </div>
                        <div class="filter-actions">
                            <button class="btn btn-primary btn--block" type="submit">Áp dụng</button>
                            <button class="btn btn-outline-primary btn--block" type="button" @click="clearFilters">Xóa lọc</button>
                        </div>
                    </form>
                </aside>
                
                <section class="catalog-content" ref="resultsRef">
                    <div class="catalog-header">
                        <h1 class="page-title">Tất cả sản phẩm</h1>
                        <p class="product-count">{{ (data.products || []).length }} sản phẩm</p>
                    </div>
                    
                    <div v-if="loading" class="status-message">Đang tải dữ liệu...</div>
                    <div v-if="error" class="status-message status-error">{{ error }}</div>
                    
                    <div class="product-grid">
                        <div class="product-card" v-for="p in (data.products || [])" :key="p.id">
                            <div class="product-card__image-wrapper">
                                <router-link :to="'/product/detail?id=' + p.id">
                                    <img class="product-card__image" :src="p.image ? '/images/' + p.image : '/images/product1.jpg'" :alt="p.name">
                                </router-link>
                            </div>
                            <div class="product-card__info">
                                <h3 class="product-card__name">
                                    <router-link :to="'/product/detail?id=' + p.id">{{ p.name }}</router-link>
                                </h3>
                                <div class="product-card__price-row">
                                    <div class="product-card__price">{{ money(finalPrice(p)) }} VNĐ</div>
                                    <div v-if="hasDiscount(p)" class="product-card__price-old">{{ money(p.price) }} VNĐ</div>
                                </div>
                                <div v-if="hasDiscount(p)" class="product-card__discount-badge">-{{ discountPercent(p) }}%</div>
                            </div>
                            <div class="product-card__actions">
                                <router-link class="btn btn-primary btn--sm btn--block" :to="'/product/detail?id=' + p.id">Xem chi tiết</router-link>
                            </div>
                        </div>
                    </div>
                    
                    <nav class="pagination-wrapper" v-if="(data.totalPages || 0) > 1">
                        <ul class="pagination">
                            <li class="page-item">
                                <button class="page-link" type="button" @click="prev">Trước</button>
                            </li>
                            <li class="page-item active">
                                <span class="page-link">[{{ (data.currentPage || 0) + 1 }}] / [{{ data.totalPages || 0 }}]</span>
                            </li>
                            <li class="page-item">
                                <button class="page-link" type="button" @click="next">Sau</button>
                            </li>
                        </ul>
                    </nav>
                </section>
            </div>
        </div>
    </main>
</template>
