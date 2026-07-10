export const productImageUrls = [
  'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1592432678016-e910b452f9a2?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1506629905607-d9ef4c469c8a?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=900&q=80'
]

export const bannerImageUrls = [
  'https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?auto=format&fit=crop&w=1600&q=80',
  'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1600&q=80',
  'https://images.unsplash.com/photo-1491933382434-500287f9b54b?auto=format&fit=crop&w=1600&q=80',
  'https://images.unsplash.com/photo-1517430816045-df4b7de11d1d?auto=format&fit=crop&w=1600&q=80'
]

export function pickProductImage(index) {
  return productImageUrls[index % productImageUrls.length]
}

export function normalizeImageSource(value, fallbackIndex = 0) {
  if (!value || typeof value !== 'string') return pickProductImage(fallbackIndex)
  const trimmed = value.trim()
  const urlMatch = trimmed.match(/^url\((['"]?)(.*?)\1\)$/i)
  if (urlMatch) return normalizeImageSource(urlMatch[2], fallbackIndex)
  if (/^https?:\/\/img\.jd-demo\.com/i.test(trimmed)) return pickProductImage(fallbackIndex)
  if (/^https?:\/\//i.test(trimmed) || trimmed.startsWith('/')) return trimmed
  if (/^linear-gradient|^radial-gradient/i.test(trimmed)) return pickProductImage(fallbackIndex)
  return trimmed
}

export function imageStyle(value, fallbackIndex = 0) {
  const src = normalizeImageSource(value, fallbackIndex)
  if (/^linear-gradient|^radial-gradient/i.test(src)) return { background: src }
  return {
    backgroundImage: `url("${src}")`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundColor: '#f7f7f7'
  }
}

export function isImageSource(value) {
  if (!value || typeof value !== 'string') return false
  const trimmed = value.trim()
  return /^https?:\/\//i.test(trimmed) || trimmed.startsWith('/') || /^url\(/i.test(trimmed)
}

export function fallbackProductSvg(label = '商品', fallbackIndex = 0) {
  const colors = [
    ['#e1251b', '#ff7a45'],
    ['#1677ff', '#69c0ff'],
    ['#52c41a', '#95de64'],
    ['#722ed1', '#b37feb'],
    ['#fa8c16', '#ffd591']
  ]
  const [from, to] = colors[Math.abs(fallbackIndex) % colors.length]
  const safeLabel = String(label || '商品').slice(0, 6)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" viewBox="0 0 600 600"><defs><linearGradient id="g" x1="0" x2="1" y1="0" y2="1"><stop offset="0" stop-color="${from}"/><stop offset="1" stop-color="${to}"/></linearGradient></defs><rect width="600" height="600" rx="36" fill="#f7f7f7"/><circle cx="300" cy="250" r="132" fill="url(#g)" opacity=".92"/><path d="M214 223h172l-19 170H233z" fill="#fff" opacity=".96"/><path d="M246 223c0-42 24-73 54-73s54 31 54 73" fill="none" stroke="#fff" stroke-width="22" stroke-linecap="round"/><text x="300" y="466" text-anchor="middle" font-family="Arial, sans-serif" font-size="42" font-weight="700" fill="#333">${safeLabel}</text></svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

export function normalizeProduct(product = {}, fallbackIndex = 0) {
  const image = normalizeImageSource(
    product.image || product.mainImage || product.main_image || product.productImage || product.product_image,
    fallbackIndex
  )
  const rawImages = product.images || product.subImages || product.sub_images
  let images = []
  if (Array.isArray(rawImages)) {
    images = rawImages
  } else if (typeof rawImages === 'string') {
    try {
      images = JSON.parse(rawImages)
    } catch {
      images = rawImages.split(',').map(item => item.trim()).filter(Boolean)
    }
  }
  return {
    ...product,
    image,
    mainImage: image,
    images: [image, ...images.map((item, index) => normalizeImageSource(item, fallbackIndex + index + 1))]
  }
}
