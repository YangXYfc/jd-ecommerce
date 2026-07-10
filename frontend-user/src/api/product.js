import request from '@/utils/request'
import { mockProducts, mockCategories, mockBanners, getProductSkus, getProductReviews } from '@/utils/mock-data'
import { normalizeProduct } from '@/utils/product-images'

const categoryIconMap = {
  1: '\uD83D\uDCF1',
  2: '\uD83C\uDFE0',
  3: '\uD83D\uDC55',
  4: '\uD83C\uDF4E',
  5: '\uD83D\uDC84',
  6: '\uD83E\uDDF8',
  7: '\uD83C\uDFC3',
  8: '\uD83D\uDCDA',
  9: '\u2328\uFE0F',
  10: '\uD83C\uDFA7'
}

function normalizeCategory(category = {}, index = 0) {
  const icon = category.icon || category.iconUrl || category.icon_url
  const fallbackIcon = categoryIconMap[category.id] || categoryIconMap[index + 1] || '\uD83D\uDED2'
  return {
    ...category,
    icon: /^https?:\/\/img\.jd-demo\.com/i.test(icon || '') ? fallbackIcon : (icon || fallbackIcon)
  }
}

function normalizeBanner(item = {}) {
  return {
    ...item,
    image: item.image || item.imageUrl || item.image_url,
    link: item.link || item.linkUrl || item.link_url || '/product-list'
  }
}

function normalizeProductListResponse(res, page, size) {
  const list = Array.isArray(res) ? res : (res.items || res.list || res.records || [])
  const items = list.map((item, index) => ({
    ...normalizeProduct(item, index),
    sales: item.sales ?? item.salesCount ?? item.sales_count ?? 0,
    stock: item.stock ?? item.totalStock ?? 0
  }))
  return Array.isArray(res)
    ? { items, total: items.length, page, size }
    : { ...res, items, total: res.total ?? items.length, page, size }
}

export function getBanners() {
  return request.get('/banners')
    .then(items => (items || []).map(normalizeBanner))
    .catch(() => mockBanners.map(normalizeBanner))
}

export function getCategories() {
  return request.get('/categories')
    .then(items => (items || []).map(normalizeCategory))
    .catch(() => mockCategories.map(normalizeCategory))
}

export function getProductList(params = {}) {
  const { page = 1, size = 20, categoryId, keyword, sortBy = 'default', minPrice, maxPrice } = params
  return request.get('/products', { params })
    .then(res => normalizeProductListResponse(res, page, size))
    .catch(() => {
      let list = [...mockProducts]
      if (categoryId) {
        list = list.filter(p => p.categoryId === Number(categoryId) || p.subCategoryId === Number(categoryId))
      }
      if (keyword) {
        const kw = keyword.toLowerCase()
        list = list.filter(p => p.name.toLowerCase().includes(kw) || p.brand.toLowerCase().includes(kw))
      }
      if (minPrice != null) list = list.filter(p => p.price >= minPrice)
      if (maxPrice != null) list = list.filter(p => p.price <= maxPrice)
      switch (sortBy) {
        case 'price_asc': list.sort((a, b) => a.price - b.price); break
        case 'price_desc': list.sort((a, b) => b.price - a.price); break
        case 'sales': list.sort((a, b) => b.sales - a.sales); break
        default: break
      }
      const total = list.length
      const start = (page - 1) * size
      const items = list.slice(start, start + size).map((item, index) => normalizeProduct(item, start + index))
      return { items, total, page, size }
    })
}

export function getProductDetail(id) {
  return request.get(`/products/${id}`)
    .catch(() => {
      const product = mockProducts.find(p => p.id === Number(id))
      if (!product) return Promise.reject(new Error('商品不存在'))
      const normalized = normalizeProduct(product, Number(id))
      return {
        ...normalized,
        skus: getProductSkus(id),
        reviews: getProductReviews(id),
        description: '本商品采用优质材料制作，支持七天无理由退换货，全国联保，正品保障。',
        images: [normalized.image, ...mockProducts.slice(0, 3).map((p, index) => normalizeProduct(p, index).image)],
        specs: [
          { label: '品牌', value: product.brand },
          { label: '商品名称', value: product.name },
          { label: '商品编号', value: `JD${String(product.id).padStart(8, '0')}` },
          { label: '产地', value: '中国大陆' },
          { label: '保修期', value: '12个月' }
        ]
      }
    })
    .then(data => ({
      ...normalizeProduct(data, Number(id)),
      skus: data.skus,
      reviews: data.reviews,
      description: data.description,
      specs: data.specs
    }))
}

export function getProductReviewsApi(productId, params = {}) {
  return request.get(`/products/${productId}/reviews`, { params })
    .catch(() => {
      const reviews = getProductReviews(productId)
      const { page = 1, size = 10, rating } = params
      let list = reviews
      if (rating) list = list.filter(r => r.rating === Number(rating))
      const total = list.length
      const start = (page - 1) * size
      return { items: list.slice(start, start + size), total, page, size }
    })
}
