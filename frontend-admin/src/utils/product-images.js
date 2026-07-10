export const productImageUrls = [
  'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80'
]

export function pickProductImage(index) {
  return productImageUrls[index % productImageUrls.length]
}

export function normalizeImageSource(value, fallbackIndex = 0) {
  if (!value || typeof value !== 'string') return pickProductImage(fallbackIndex)
  const trimmed = value.trim()
  if (/^https?:\/\//i.test(trimmed) || trimmed.startsWith('/')) return trimmed
  if (/^linear-gradient|^radial-gradient/i.test(trimmed)) return pickProductImage(fallbackIndex)
  return trimmed
}

export function imageStyle(value, fallbackIndex = 0) {
  const src = normalizeImageSource(value, fallbackIndex)
  return {
    backgroundImage: `url("${src}")`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundColor: '#f7f7f7'
  }
}
