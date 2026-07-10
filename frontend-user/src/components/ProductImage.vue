<template>
  <div class="product-image-box" role="img" :aria-label="alt">
    <img class="product-image-img" :src="currentSrc" :alt="alt" @error="useFallback" />
    <slot />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { fallbackProductSvg, normalizeImageSource } from '@/utils/product-images'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '商品图片' },
  fallbackIndex: { type: Number, default: 0 }
})

const failed = ref(false)
const normalizedSrc = computed(() => normalizeImageSource(props.src, props.fallbackIndex))
const fallbackSrc = computed(() => fallbackProductSvg(props.alt, props.fallbackIndex))
const currentSrc = computed(() => (failed.value ? fallbackSrc.value : normalizedSrc.value))

watch(() => props.src, () => {
  failed.value = false
})

function useFallback() {
  failed.value = true
}
</script>

<style scoped>
.product-image-box {
  position: relative;
  overflow: hidden;
  background: #f7f7f7;
}
.product-image-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
</style>
