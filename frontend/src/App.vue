<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
import ChatBox from "@/components/chat/ChatBox.vue";

const noticeOpen = ref(false);
const noticeText = ref("");
const noticeType = ref("info");
const lastSignature = ref("");
let observer = null;
let debounceTimer = null;

const detectType = (el) => {
    const cls = String(el?.className || "");
    if (cls.includes("status-error") || cls.includes("alert-danger")) return "error";
    if (cls.includes("status-success") || cls.includes("alert-success")) return "success";
    if (cls.includes("alert-info")) return "info";
    return "info";
};

const collectInlineNotice = () => {
    const selectors = [
        ".status-message",
        ".alert.alert-danger",
        ".alert.alert-success",
        ".alert.alert-info"
    ];
    const nodes = document.querySelectorAll(selectors.join(","));
    for (const node of nodes) {
        if (!node || node.closest(".global-notice-panel")) {
            continue;
        }
        const text = String(node.textContent || "").trim();
        if (!text) {
            continue;
        }
        return {text, type: detectType(node)};
    }
    return null;
};

const scanAndOpen = () => {
    const found = collectInlineNotice();
    if (!found) return;
    const signature = `${found.type}::${found.text}`;
    if (signature === lastSignature.value) return;
    lastSignature.value = signature;
    noticeText.value = found.text;
    noticeType.value = found.type;
    noticeOpen.value = true;
};

const scheduleScan = () => {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(scanAndOpen, 60);
};

const closeNotice = () => {
    noticeOpen.value = false;
};

onMounted(() => {
    scheduleScan();
    observer = new MutationObserver(() => scheduleScan());
    observer.observe(document.body, {subtree: true, childList: true, characterData: true});
});

onBeforeUnmount(() => {
    if (observer) observer.disconnect();
    if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<template>
    <AppHeader/>
    <div class="app-main">
        <router-view/>
    </div>
    <div class="modal-backdrop" :class="{open: noticeOpen}" v-if="noticeOpen" @click.self="closeNotice">
        <div class="admin-modal-panel global-notice-panel" style="max-width:520px;">
            <h4 class="global-notice-title" :class="'is-' + noticeType">
                {{ noticeType === "error" ? "Lỗi" : (noticeType === "success" ? "Thành công" : "Thông báo") }}
            </h4>
            <p class="global-notice-text">{{ noticeText }}</p>
            <div class="global-notice-actions">
                <button class="btn btn-primary" type="button" @click="closeNotice">Đã hiểu</button>
            </div>
        </div>
    </div>
    <ChatBox/>
    <AppFooter/>
</template>

<style>
.status-message,
.alert.alert-danger,
.alert.alert-success,
.alert.alert-info{
    display:none !important;
}
.global-notice-title{
    margin:0;
    font-size:22px;
    font-weight:800;
}
.global-notice-title.is-error{color:#b91c1c}
.global-notice-title.is-success{color:#166534}
.global-notice-title.is-info{color:#1f2937}
.global-notice-text{
    margin:10px 0 0;
    font-size:16px;
    line-height:1.6;
    color:#1f2937;
}
.global-notice-actions{
    margin-top:16px;
    display:flex;
    justify-content:flex-end;
}
</style>
