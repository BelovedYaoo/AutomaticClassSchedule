<script setup lang="ts">
import { useLayout } from '@/service/layout';

const { isDarkTheme } = useLayout();

const getAlpha = () => {
    const alpha = isDarkTheme.value ? 0.8 : 0.4;
    return `background: linear-gradient(180deg, rgba(247, 149, 48, ${alpha}) 10%, rgba(247, 149, 48, 0) 30%)`;
};
</script>

<template>
    <div
        class="surface-ground flex align-items-center justify-content-center min-h-screen min-w-screen overflow-hidden">
        <div class="flex flex-column align-items-center justify-content-center">
            <div :style="getAlpha()" style="border-radius: 56px; padding: 0.3rem">
                <div class="w-full surface-card py-8 px-5 sm:px-8 flex flex-column align-items-center"
                     style="border-radius: 53px">
                    <div class="grid flex flex-column align-items-center">
                        <div class="flex justify-content-center align-items-center border-circle mt-3"
                             style="width: 3.2rem; height: 3.2rem">
                            <img alt="Sakai logo" class="mb-5 w-6rem flex-shrink-0" src="/images/logo.svg"/>
                        </div>
                        <h1 class="text-900 font-bold text-4xl lg:text-5xl mb-2">拒绝访问</h1>
                        <span class="text-600 mb-5 mt-2">您没有必要的权限，请联系管理员</span>
                        <img alt="Access denied" class="mb-1" src="/images/access.svg" width="80%"/>
                        <div class="col-12 mt-2 text-center">
                            <i class="pi pi-fw pi-arrow-left text-blue-500 mr-2" style="vertical-align: center"></i>
                            <router-link class="text-blue-500" to="/">返回主页面</router-link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
