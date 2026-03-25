import {createRouter, createWebHistory} from "vue-router";
import LoginView from "@/views/auth/LoginView.vue";
import HomeView from "@/views/home/HomeView.vue";
import ProductListView from "@/views/product/ProductListView.vue";
import ProductDetailView from "@/views/product/ProductDetailView.vue";
import CartView from "@/views/cart/CartView.vue";
import CheckoutView from "@/views/order/CheckoutView.vue";
import BankTransferView from "@/views/order/BankTransferView.vue";
import OrderListView from "@/views/order/OrderListView.vue";
import OrderDetailView from "@/views/order/OrderDetailView.vue";
import MyProductListView from "@/views/order/MyProductListView.vue";
import SignUpView from "@/views/account/SignUpView.vue";
import EditProfileView from "@/views/account/EditProfileView.vue";
import ChangePasswordView from "@/views/account/ChangePasswordView.vue";
import ForgotPasswordView from "@/views/account/ForgotPasswordView.vue";
import AdminAccountView from "@/views/admin/AdminAccountView.vue";
import AdminCategoryView from "@/views/admin/AdminCategoryView.vue";
import AdminProductView from "@/views/admin/AdminProductView.vue";
import AdminOrderView from "@/views/admin/AdminOrderView.vue";
import AdminRevenueView from "@/views/admin/AdminRevenueView.vue";
import AdminVipView from "@/views/admin/AdminVipView.vue";
import AdminCameraView from "@/views/admin/AdminCameraView.vue";
import {api} from "@/api";

export const routes = [
    {path: "/auth/login", component: LoginView, meta: {title: "auth/login", template: "auth/login.html"}},
    {path: "/home/index", component: HomeView, meta: {title: "home/index", template: "home/index.html"}},
    {path: "/product/list", component: ProductListView, meta: {title: "product/list", template: "product/list.html"}},
    {path: "/product/detail", component: ProductDetailView, meta: {title: "product/detail", template: "product/detail.html"}},
    {path: "/cart/index", component: CartView, meta: {title: "cart/index", template: "cart/index.html"}},
    {path: "/order/check-out", component: CheckoutView, meta: {title: "order/check-out", template: "order/check-out.html"}},
    {path: "/order/bank-transfer", component: BankTransferView, meta: {title: "order/bank-transfer", template: "order/bank-transfer.html"}},
    {path: "/order/order-list", component: OrderListView, meta: {title: "order/order-list", template: "order/order-list.html"}},
    {path: "/order/order-detail", component: OrderDetailView, meta: {title: "order/order-detail", template: "order/order-detail.html"}},
    {path: "/order/my-product-list", component: MyProductListView, meta: {title: "order/my-product-list", template: "order/my-product-list.html"}},
    {path: "/account/sign-up", component: SignUpView, meta: {title: "account/sign-up", template: "account/sign-up.html"}},
    {path: "/account/edit-profile", component: EditProfileView, meta: {title: "account/edit-profile", template: "account/edit-profile.html"}},
    {path: "/account/change-password", component: ChangePasswordView, meta: {title: "account/change-password", template: "account/change-password.html"}},
    {path: "/account/forgot-password", component: ForgotPasswordView, meta: {title: "account/forgot-password", template: "account/forgot-password.html"}},
    {path: "/admin/account", component: AdminAccountView, meta: {title: "admin/account", template: "admin/account.html"}},
    {path: "/admin/category", component: AdminCategoryView, meta: {title: "admin/category", template: "admin/category.html"}},
    {path: "/admin/product", component: AdminProductView, meta: {title: "admin/product", template: "admin/product.html"}},
    {path: "/admin/order", component: AdminOrderView, meta: {title: "admin/order", template: "admin/order.html"}},
    {path: "/admin/revenue", component: AdminRevenueView, meta: {title: "admin/revenue", template: "admin/revenue.html"}},
    {path: "/admin/vip", component: AdminVipView, meta: {title: "admin/vip", template: "admin/vip.html"}},
    {path: "/admin/camera", component: AdminCameraView, meta: {title: "admin/camera", template: "admin/camera.html"}},
    {path: "/", redirect: "/home/index"},
    {path: "/:pathMatch(.*)*", redirect: "/home/index"}
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach(async (to) => {
    if (!to.path.startsWith("/admin")) {
        return true;
    }
    try {
        const me = (await api.auth.me()).data || {};
        const roles = me.roles || [];
        const isAdmin = roles.some((role) => {
            if (typeof role === "string") {
                return role === "ADMIN" || role === "ROLE_ADMIN";
            }
            if (role && typeof role === "object") {
                const value = role.id || role.name || role.authority || role.role || "";
                return value === "ADMIN" || value === "ROLE_ADMIN";
            }
            return false;
        });
        if (isAdmin) {
            return true;
        }
        return {path: "/auth/login", query: {redirect: to.fullPath}};
    } catch (e) {
        return {path: "/auth/login", query: {redirect: to.fullPath}};
    }
});

export default router;
