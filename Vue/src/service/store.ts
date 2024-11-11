import { defineStore, StoreGeneric } from 'pinia';
import { storeState } from '@/service/store';

export interface storeState extends StoreGeneric {
    windowWidth: number
    windowHeight: number
}

export const useMainStore = defineStore('main', {
    state: (): storeState => {
        return {
            windowWidth: 1920,
            windowHeight: 1080
        };
    },
    actions: {},
});