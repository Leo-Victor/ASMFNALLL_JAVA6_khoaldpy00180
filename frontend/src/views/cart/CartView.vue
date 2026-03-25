<script setup>
import {CartPage} from "@/legacy/pages";

const {state, error, loading, updateItem, removeItem, clear, money} = CartPage.setup();
const minus = async (item) => {
    if ((item.quantity || 1) > 1) {
        item.quantity -= 1;
        await updateItem(item);
    }
};
const plus = async (item) => {
    item.quantity = (item.quantity || 0) + 1;
    await updateItem(item);
};
</script>

<template>
    <main class="cart-page">
        <div class="container">
            <h1 class="page-title">Giỏ hàng của bạn</h1>
            
            <div v-if="loading" class="status-message">Đang tải...</div>
            <div v-if="error" class="status-message status-error">{{ error }}</div>
            
            <div v-if="!state.items.length && !loading" class="empty-cart">
                <div class="empty-cart-icon">🛒</div>
                <h3>Giỏ hàng trống</h3>
                <p>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
                <router-link class="btn btn-primary" to="/product/list">Khám phá sản phẩm</router-link>
            </div>
            
            <div class="cart-content" v-if="state.items.length">
                <div class="cart-table-wrap">
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Giá</th>
                                <th>Số lượng</th>
                                <th>Size</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in state.items" :key="item.productId + '-' + item.sizeId">
                                <td>
                                    <div class="cart-item-product">
                                        <img :src="item.image ? '/images/' + item.image : '/images/product1.jpg'" :alt="item.name" width="80" height="80" class="cart-item-image">
                                        <strong>{{ item.name }}</strong>
                                    </div>
                                </td>
                                <td>{{ money(item.price) }} VNĐ</td>
                                <td>
                                    <div class="cart-item-qty-form">
                                        <button class="btn btn-outline-secondary btn-sm" type="button" @click="minus(item)">-</button>
                                        <input type="number" min="1" v-model.number="item.quantity" class="cart-item-qty-input" @change="updateItem(item)">
                                        <button class="btn btn-outline-secondary btn-sm" type="button" @click="plus(item)">+</button>
                                    </div>
                                </td>
                                <td>{{ item.sizeName }}</td>
                                <td>
                                    <button class="btn btn-action-solid btn-sm" type="button" @click="removeItem(item)">Xóa</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                
                <div class="cart-summary">
                    <div class="cart-summary-content">
                        <div class="cart-summary-row">
                            <span>Tổng tiền:</span>
                            <strong class="cart-total">{{ money(state.totalPrice) }} VNĐ</strong>
                        </div>
                    </div>
                    <div class="cart-summary-actions">
                        <button class="btn btn-outline-secondary" type="button" @click="clear">Xóa tất cả</button>
                        <router-link class="btn checkout-btn" to="/order/check-out">Thanh toán</router-link>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>
