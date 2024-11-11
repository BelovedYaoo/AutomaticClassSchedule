<script lang="ts" setup>
import { FilterMatchMode } from 'primevue/api';
import { computed, onBeforeMount, ref, watch } from 'vue';
import {
    DataTablePageEvent,
    DataTableRowContextMenuEvent,
    DataTableRowReorderEvent,
    DataTableSortEvent
} from 'primevue/datatable';
import { useLayout } from '@/service/layout';
import { storeState, useMainStore } from '@/service/store';
import { storeToRefs } from 'pinia';
import { useToast } from 'primevue/usetoast';

const toast = useToast();

interface customTableProps {
    tableName: string,
    tableData: Array<BaseFiled>,
    onTableDataRefresh: () => void,
    onRowReorder: (event: DataTableRowReorderEvent) => void,
    onRowModify: (record: BaseFiled) => void,
    onRowDelete: (record: BaseFiled[]) => void,
    onOrderSwap: (record: BaseFiled[]) => void,
}

const props = defineProps<customTableProps>();

// 挂载之前
onBeforeMount(() => {
    initFilters();
});

// 过滤器
const keywordFilters = ref();
const initFilters = () => {
    keywordFilters.value = {
        global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    };
};

// 启用排序和选中
const enableSortedAndSelected = ref(false);
const switchedAndSelectedToggle = () => {
    enableSortedAndSelected.value = !enableSortedAndSelected.value;
    selectedRecords.value = [];
};

const selectedRecords = ref([]);
const contextMenuSelection = ref();

// 样式设置浮窗
const paletteOp = ref();
const paletteToggle = (event: PointerEvent) => {
    paletteOp.value.toggle(event);
};

// 表格大小选项
const sizeOptions = ref([
    { label: '较小', value: 'small', class: 'sm' },
    { label: '默认', value: 'normal', class: '' },
    { label: '较大', value: 'large', class: 'lg' }
]);

// 表格样式选项
const dataTableStyle = ref({
    size: { label: '默认', value: 'normal', class: '' },
    showGridlines: false,
    stripedRows: true,
    rowsPerPage: 10,
    currentPage: 1,
    totalPages: 0
});

// 总页数
const totalPages = ref(0);

// 总记录数
const totalRecords = ref(0);

// 监听页面数量变化
watch(() => dataTableStyle.value.rowsPerPage, (newValue, oldValue) => {
    // 修正当前页数
    dataTableStyle.value.currentPage = Math.max(Math.ceil(((dataTableStyle.value.currentPage - 1) * oldValue + 1) / newValue), 1);
    correctTotalPages();
});

// 监听表格数据变化
watch(() => props.tableData, () => {
    // 修正总记录数
    totalRecords.value = props.tableData.length;
    correctTotalPages();
});

// 修正总页数
const correctTotalPages = () => {
    totalPages.value = Math.ceil(props.tableData.length / dataTableStyle.value.rowsPerPage);
};

// 当通过表格分页器切换分页时，通知底部分页器同步
const onPageChange = (event: DataTablePageEvent) => {
    dataTableStyle.value.currentPage = event.page + 1;
};

// 从状态仓库取出窗口宽度与高度
const store = useMainStore();
const { windowWidth, windowHeight } = storeToRefs<storeState>(store);

const { layoutState } = useLayout();

// 表格底部显示
const showFooter = computed(() => {
    return windowWidth.value > (!layoutState.staticMenuDesktopInactive.value ? 1388 : 1110);
});

// 迷你显示
const miniShow = computed(() => {
    return windowWidth.value < 563;
});

// 表数据导出
const dataTable = ref();
const exportCSV = () => {
    dataTable.value.exportCSV(enableSortedAndSelected.value ? { selectionOnly: true } : undefined);
};

// 表右键菜单
const cm = ref();

// 菜单展开
const onRowContextMenu = (event: DataTableRowContextMenuEvent) => {
    cm.value.show(event.originalEvent);
};

// 菜单列表
const menuModel = computed(() => {
    return [
        {
            label: '修改',
            icon: 'pi pi-fw pi-pencil',
            command: () => modifyRecord(contextMenuSelection.value)
        },
        {
            label: '删除',
            icon: 'pi pi-fw pi-times',
            command: () => deleteRecords([contextMenuSelection.value])
        }
    ].concat((enableSortedAndSelected.value && selectedRecords.value.length > 0) ? [
        {
            label: '删除选中项',
            icon: 'pi pi-fw pi-trash',
            command: () => deleteRecords(selectedRecords.value)
        }
    ] : [])
        .concat((enableSortedAndSelected.value && selectedRecords.value.length === 2) ? [
            {
                label: '交换顺序',
                icon: 'pi pi-fw pi-sync',
                command: () => orderSwap(selectedRecords.value)
            }
        ] : []);
});

// 移除右键菜单的隐藏元素
const onContextMenuShow = () => {
    const menuLinks = document.getElementsByClassName('p-menuitem-link');
    for (let i = 0; i < menuLinks.length; i++) {
        menuLinks[i].removeAttribute('aria-hidden');
    }
};

// 刷新逻辑
const onTableDataRefresh = () => {
    props.onTableDataRefresh();
};

// 修改逻辑
const modifyRecord = (modifyRecord: BaseFiled) => {
    props.onRowModify(modifyRecord);
};

// 删除逻辑
const deleteRecords = (deleteRecords: BaseFiled[]) => {
    props.onRowDelete(deleteRecords);
    // 从 selectedRecords 中去掉 deleteRecords 中的
    selectedRecords.value = selectedRecords.value.filter(selectedRecord => !deleteRecords.includes(selectedRecord));
};

// 字段排序事件
const inSort = ref();
const onSort = (event: DataTableSortEvent) => {
    inSort.value = event.multiSortMeta.length > 0;
};

// 行排序事件
const onRowReorder = (event: DataTableRowReorderEvent) => {
    if (inSort.value) {
        toast.add({
            severity: 'error',
            summary: '排序失败',
            detail: '按字段排序时，不允许对行排序',
            life: 3000
        });
        return;
    }
    props.onRowReorder(event);
};

// 顺序交换逻辑
const orderSwap = (swapRecords: BaseFiled[]) => {
    props.onOrderSwap(swapRecords);
    selectedRecords.value = [];
};

</script>

<template>
    <ContextMenu ref="cm" :model="menuModel" @show="onContextMenuShow"/>
    <DataTable ref="dataTable"
               v-model:contextMenuSelection="contextMenuSelection"
               v-model:selection="selectedRecords"
               :class="`p-datatable-${dataTableStyle.size.class}`"
               :filters="keywordFilters"
               :first="(dataTableStyle.currentPage - 1) * dataTableStyle.rowsPerPage"
               :paginator-template="{
                       '450px': 'PrevPageLink CurrentPageReport NextPageLink',
                       '570px': 'FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink'
                   }"
               :reorderableColumns="enableSortedAndSelected"
               :rows="dataTableStyle.rowsPerPage"
               :scrollHeight="`${windowHeight - 400}px`"
               :showGridlines="dataTableStyle.showGridlines"
               :stripedRows="dataTableStyle.stripedRows"
               :value="props.tableData"
               contextMenu
               current-page-report-template="{currentPage} / {totalPages}"
               paginator
               removableSort
               scrollable
               sortMode="multiple"
               tableStyle="min-width: 50rem"
               @page="onPageChange"
               @rowContextmenu="onRowContextMenu"
               @rowReorder="onRowReorder"
               @sort="onSort">
        <template #header>
            <div class="flex flex-wrap align-items-center justify-content-between gap-2">
                <span class="text-xl text-900 font-bold">{{ props.tableName }}</span>
                <div class="flex gap-4">
                        <span class="p-input-icon-left">
                            <i class="pi pi-search"/>
                            <InputText v-model="keywordFilters['global'].value" :class="miniShow?'w-10rem':'w-13rem'"
                                       placeholder="输入以搜索" type="text"/>
                        </span>
                    <!-- 刷新按钮 -->
                    <Button v-if="!miniShow" icon="pi pi-refresh" raised rounded @click="onTableDataRefresh"/>
                    <!-- 修改按钮 -->
                    <Button v-if="!miniShow" icon="pi pi-bars" raised rounded
                            @click="switchedAndSelectedToggle"/>
                    <!-- 配置按钮 -->
                    <Button icon="pi pi-cog" raised rounded @click="paletteToggle"/>
                    <OverlayPanel ref="paletteOp">
                        <div class="flex flex-column gap-3">
                            <div class="flex flex-row gap-3">
                                <!-- 刷新按钮 -->
                                <Button v-if="miniShow" icon="pi pi-refresh" raised rounded
                                        @click="onTableDataRefresh"/>
                                <!-- 修改按钮 -->
                                <Button v-if="miniShow" icon="pi pi-bars" raised rounded
                                        @click="switchedAndSelectedToggle"/>
                                <Button icon="pi pi-upload" raised rounded
                                        @click="exportCSV"/>
                            </div>
                            <SelectButton v-model="dataTableStyle.size" :options="sizeOptions" dataKey="label"
                                          optionLabel="label"/>
                            <div class="flex align-items-center justify-content-between">
                                <label>单元格描边：</label>
                                <InputSwitch v-model="dataTableStyle.showGridlines"/>
                            </div>
                            <div class="flex align-items-center justify-content-between">
                                <label>交替条纹：</label>
                                <InputSwitch v-model="dataTableStyle.stripedRows"/>
                            </div>
                        </div>
                        <Divider v-if="!showFooter"/>
                        <div class="flex flex-column gap-3">
                            <div v-if="!showFooter" class="flex flex-column gap-3">
                                <div>
                                    <label>单页数量：</label>
                                    <Dropdown v-model="dataTableStyle.rowsPerPage" :options="[5, 10, 20, 50]"/>
                                </div>
                                <div>
                                    <label>当前页数：</label>
                                    <Dropdown v-model="dataTableStyle.currentPage"
                                              :options="Array.from({length: totalPages}, (_, i) => i + 1)"/>
                                </div>
                            </div>
                        </div>
                    </OverlayPanel>
                </div>
            </div>
        </template>
        <template v-if="showFooter" #paginatorstart>
            <label>当前显示第 {{ (dataTableStyle.currentPage - 1) * dataTableStyle.rowsPerPage + 1 }} 至
                {{ Math.min(dataTableStyle.currentPage * dataTableStyle.rowsPerPage, totalRecords) }} 项，共
                {{ totalRecords }}
                条记录</label>
        </template>
        <template v-if="showFooter" #paginatorend>
            <div class="flex flex-row align-items-center justify-content-center">
                <div>
                    <label>单页数量：</label>
                    <Dropdown v-model="dataTableStyle.rowsPerPage" :options="[5, 10, 20, 50]"/>
                </div>
                <div>
                    <label>当前页数：</label>
                    <Dropdown v-model="dataTableStyle.currentPage"
                              :options="Array.from({length: totalPages}, (_, i) => i + 1)"/>
                </div>
            </div>
        </template>
        <Column v-if="enableSortedAndSelected" :reorderableColumn="false" headerStyle="width:1%;min-width:1rem;"
                rowReorder/>
        <Column v-if="enableSortedAndSelected" :reorderableColumn="false" headerStyle="width:1%;min-width:1rem;"
                selectionMode="multiple"></Column>
        <slot name="column"></slot>
    </DataTable>
</template>

<style scoped>
.edit-button {
    width: 30px;
    height: 30px;
}
</style>