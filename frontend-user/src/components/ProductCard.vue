<template>
  <div class="product-card" @click="goDetail">
    <div class="product-image" :style="{ background: product.image }">
      <span v-if="product.tags" class="product-tags">
        <span v-for="tag in product.tags" :key="tag" class="tag">{{ tag }}</span>
      </span>
    </div>
    <div class="product-info">
      <p class="product-name text-ellipsis-2">{{ product.name }}</p>
      <div class="product-meta">
        <span class="shop-name">{{ product.merchantName }}</span>
        <span class="sales">已售{{ product.sales > 9999 ? '1万+' : product.sales }}件</span>
      </div>
      <div class="product-price-row">
        <div class="price">
          <span class="price-symbol">¥</span>
          <span class="price-integer">{{ Math.floor(product.price) }}</span>
          <span class="price-decimal">.00</span>
        </div>
        <span v-if="product.rating" class="rating">⭐{{ product.rating }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const props = defineProps({ product: { type: Object, required: true } })
const router = useRouter()
function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}
.product-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--jd-red);
}
.product-image {
  width: 100%;
  aspect-ratio: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-tags {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  gap: 4px;
}
.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  background: rgba(225, 37, 27, 0.9);
  color: #fff;
}
.product-info {
  padding: 10px 12px;
}
.product-name {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.4;
  height: 36px;
  margin-bottom: 6px;
}
.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.product-price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.rating {
  font-size: 11px;
  color: var(--text-secondary);
}
</style>
