<script setup>
defineProps({
    displayHeight: {
        type: String,
        required: false,
        default: '50'
    },
    marginBottom: {
        type: String,
        required: false,
        default: 'mb-3'
    }
});
</script>

<template>
    <img :class="marginBottom" :height="displayHeight" alt="Image" src="/images/logo.svg"/>
</template>

<style lang="scss" scoped></style>
