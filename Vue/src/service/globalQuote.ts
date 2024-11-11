import { AxiosResponse } from 'axios';
import { ToastMessageOptions } from 'primevue/toast';
import { AppConfig } from '@/service/globalQuote';

/**
 * 将Axios请求响应体转换为PrimeVue的Toast配置
 * @param response Axios响应体
 * @param duringSecond Toast显示时长(默认3秒)
 */
export const responseToastConfig = (response: AxiosResponse<any>, duringSecond: number = 3): ToastMessageOptions => {
    return {
        severity: response.data.code === 200 ? 'success' : 'error',
        summary: response.data.message,
        detail: response.data.description,
        life: duringSecond * 1000
    };
};

/**
 * 将类名添加到指定id的元素上，并设置延时移除
 * @param id 需要添加的元素id名
 * @param className 被添加的类名
 * @param duringSecond 延时时长(默认5秒延时)
 */
export const addClassById = (id: string, className: string, duringSecond: number = 5): void => {
    const element: HTMLElement | null = document.getElementById(id);
    if (element) {
        element.classList.add(className);
        setTimeout((): void => {
            element.classList.remove(className);
        }, duringSecond * 1000);
    }
};

export const getParameterByName = (name, url = window.location.href) => {
    name = name.replace(/[\[\]]/g, '\\$&');
    const regex = new RegExp(`[?&]${  name  }(=([^&#]*)|&|#|$)`),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
};

/**
 * 全局配置
 */
export const globalConfig: AppConfig = {
    // 应用名称
    appName: 'ACS Server',
    // Token名称
    appTokenName: 'acsToken',
    // 查询所有URL
    queryAllUrl: '/queryAll',
    // 更新URL
    updateUrl: '/update',
    // 删除URL
    deleteUrl: '/delete',
    // 新增URL
    addUrl: '/add',
    // 重新排序URL
    reorderUrl: '/reorder',
};