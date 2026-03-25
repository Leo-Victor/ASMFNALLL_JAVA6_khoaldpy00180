<script setup>
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
</script>

<template>
    <AppHeader/>
    <div class="app-main">
        <router-view/>
    </div>
    <AppFooter/>
</template>
