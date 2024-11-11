<script setup lang="ts">
import { globalConfig } from '@/service/globalQuote';
</script>

<template>
    <div class="layout-footer">
        <img alt="Logo" class="mr-2" height="20" src="/images/logo.svg"/>
        by
        <span class="font-medium ml-2">{{ globalConfig.appName }}</span>
    </div>
</template>
<style lang="scss" scoped></style>
