/**
 * 商品相关 API
 * 优先调用后端接口，后端不可用时降级到 mock 数据
 */
import request from '@/utils/request'
import { mockProducts, mockCategories, mockBanners, getProductSkus, getProductReviews } from '@/utils/mock-data'

// 获取轮播图
export function getBanners() {
  return request.get('/banners').catch(() => mockBanners)
}

// 获取分类列表
export function getCategories() {
  return request.get('/categories').catch(() => mockCategories)
}

// 获取商品列表（分页、筛选、搜索）
export function getProductList(params = {}) {
  const { page = 1, size = 20, categoryId, keyword, sortBy = 'default', minPrice, maxPrice } = params
  return request.get('/products', { params }).catch(() => {
    let list = [...mockProducts]
    // 分类筛选
    if (categoryId) {
      list = list.filter(p => p.categoryId === Number(categoryId) || p.subCategoryId === Number(categoryId))
    }
    // 搜索
    if (keyword) {
      const kw = keyword.toLowerCase()
      list = list.filter(p => p.name.toLowerCase().includes(kw) || p.brand.toLowerCase().includes(kw))
    }
    // 价格区间
    if (minPrice != null) list = list.filter(p => p.price >= minPrice)
    if (maxPrice != null) list = list.filter(p => p.price <= maxPrice)
    // 排序
    switch (sortBy) {
      case 'price_asc': list.sort((a, b) => a.price - b.price); break
      case 'price_desc': list.sort((a, b) => b.price - a.price); break
      case 'sales': list.sort((a, b) => b.sales - a.sales); break
      default: break
    }
    // 分页
    const total = list.length
    const start = (page - 1) * size
    const items = list.slice(start, start + size)
    return { items, total, page, size }
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request.get(`/products/${id}`).catch(() => {
    const product = mockProducts.find(p => p.id === Number(id))
    if (!product) return Promise.reject(new Error('商品不存在'))
    return {
      ...product,
      skus: getProductSkus(id),
      reviews: getProductReviews(id),
      description: '【产品详情】本产品采用优质材料精心制作，工艺精湛，品质可靠。支持七天无理由退换货，全国联保，正品保障。',
      images: [product.image, ...mockProducts.slice(0, 3).map(p => p.image)],
      specs: [
        { label: '品牌', value: product.brand },
        { label: '商品名称', value: product.name },
        { label: '商品编号', value: `JD${String(product.id).padStart(8, '0')}` },
        { label: '产地', value: '中国大陆' },
        { label: '保修期', value: '12个月' }
      ]
    }
  })
}

// 获取商品评价
export function getProductReviewsApi(productId, params = {}) {
  return request.get(`/products/${productId}/reviews`, { params }).catch(() => {
    const reviews = getProductReviews(productId)
    const { page = 1, size = 10, rating } = params
    let list = reviews
    if (rating) list = list.filter(r => r.rating === Number(rating))
    const total = list.length
    const start = (page - 1) * size
    return { items: list.slice(start, start + size), total, page, size }
  })
}
