<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useLayout } from '@/service/layout';
import { globalConfig } from '@/service/globalQuote';

const { switchDarkTheme, layoutConfig, onMenuToggle } = useLayout();

const outsideClickListener = ref();
const topbarMenuActive = ref(false);

onMounted(() => {
    bindOutsideClickListener();
});

onBeforeUnmount(() => {
    unbindOutsideClickListener();
});

const onTopBarMenuButton = () => {
    topbarMenuActive.value = !topbarMenuActive.value;
};
const topbarMenuClasses = computed(() => {
    return {
        'layout-topbar-menu-mobile-active': topbarMenuActive.value
    };
});

const bindOutsideClickListener = () => {
    if (!outsideClickListener.value) {
        outsideClickListener.value = (event:PointerEvent) => {
            if (isOutsideClicked(event)) {
                topbarMenuActive.value = false;
            }
        };
        document.addEventListener('click', outsideClickListener.value);
    }
};
const unbindOutsideClickListener = () => {
    if (outsideClickListener.value) {
        document.removeEventListener('click', outsideClickListener.value);
        outsideClickListener.value = null;
    }
};
const isOutsideClicked = (event:PointerEvent) => {
    if (!topbarMenuActive.value) return;

    const sidebarEl = document.querySelector('.layout-topbar-menu') as Node;
    const topbarEl = document.querySelector('.layout-topbar-menu-button') as Node;

    return !(sidebarEl.isSameNode(event.target as Node) || sidebarEl.contains(event.target as Node) || topbarEl.isSameNode(event.target as Node) || topbarEl.contains(event.target as Node));
};
const logoUrl = computed(() => {
    return `images/${layoutConfig.darkTheme.value ? 'day' : 'night'}.svg`;
});
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo" @click="onMenuToggle()">
            <img alt="logo" src="/images/logo.svg" />
            <span class="pl-2">{{ globalConfig.appName }}</span>
        </div>

        <button class="p-link layout-menu-button layout-topbar-button ayou" @click="onMenuToggle()">
            <i class="pi pi-bars"></i>
        </button>

        <button class="p-link layout-topbar-menu-button layout-topbar-button" @click="onTopBarMenuButton()">
            <i class="pi pi-ellipsis-v"></i>
        </button>

        <div :class="topbarMenuClasses" class="gap-3 layout-topbar-menu align-items-center">
            <Button class="m-0 layout-menu-button justify-center" style="padding: 7.7px" rounded text
                @click="switchDarkTheme()">
                <img :src="logoUrl" alt="s" height="20" width="20" />
            </Button>
            <Button aria-label="Bookmark" class="m-0 p-0 layout-menu-button topbar-button" icon="pi pi-bookmark" rounded
                severity="secondary" size="large" text />
            <Button aria-label="Search" class="m-0 p-0 layout-menu-button topbar-button" icon="pi pi-search" rounded
                severity="success" size="large" text />
            <Avatar image="https://avatars.githubusercontent.com/u/103827252?v=4" class="" size="large"
                style="background-color: #ece9fc; color: #2a1261" shape="circle" />
        </div>
    </div>
</template>

<style lang="scss" scoped>
.topbar-button {
    width: 36.6px;
    height: 36.6px;
}
</style>
