<script lang="ts" setup>

import AppMenuItem from './AppMenuItem.vue';
import { ref } from 'vue';

const model = ref([
    {
        label: 'Home',
        items: [
            {
                label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/'
            }
        ]
    },
    {
        label: 'Pages',
        icon: 'pi pi-fw pi-briefcase',
        to: '/pages',
        items: [
            {
                label: 'Not Found',
                icon: 'pi pi-fw pi-exclamation-circle',
                to: '/notfound'
            },
            {
                label: 'Error',
                icon: 'pi pi-fw pi-times-circle',
                to: '/error'
            },
            {
                label: 'Access Denied',
                icon: 'pi pi-fw pi-lock',
                to: '/access'
            }
        ]
    }
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item :index="i" :item="item"/>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
