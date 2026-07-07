/**
 * Mock 数据 - 模拟后端 API 响应
 * 在后端未就绪时提供完整的前端开发数据
 */

// 商品分类
export const mockCategories = [
  { id: 1, name: '手机数码', icon: '📱', children: [
    { id: 11, name: '手机通讯' },
    { id: 12, name: '手机配件' },
    { id: 13, name: '数码配件' },
    { id: 14, name: '影音娱乐' }
  ]},
  { id: 2, name: '电脑办公', icon: '💻', children: [
    { id: 21, name: '笔记本' },
    { id: 22, name: '台式机' },
    { id: 23, name: '电脑配件' },
    { id: 24, name: '办公设备' }
  ]},
  { id: 3, name: '家用电器', icon: '🔌', children: [
    { id: 31, name: '大家电' },
    { id: 32, name: '厨房电器' },
    { id: 33, name: '生活电器' },
    { id: 34, name: '个护健康' }
  ]},
  { id: 4, name: '服饰内衣', icon: '👕', children: [
    { id: 41, name: '男装' },
    { id: 42, name: '女装' },
    { id: 43, name: '内衣' },
    { id: 44, name: '配饰' }
  ]},
  { id: 5, name: '鞋靴箱包', icon: '👜', children: [
    { id: 51, name: '男鞋' },
    { id: 52, name: '女鞋' },
    { id: 53, name: '箱包' }
  ]},
  { id: 6, name: '美妆护肤', icon: '💄', children: [
    { id: 61, name: '面部护肤' },
    { id: 62, name: '彩妆' },
    { id: 63, name: '香水' }
  ]},
  { id: 7, name: '食品生鲜', icon: '🍎', children: [
    { id: 71, name: '零食' },
    { id: 72, name: '生鲜' },
    { id: 73, name: '饮料' }
  ]},
  { id: 8, name: '母婴玩具', icon: '🧸', children: [
    { id: 81, name: '奶粉' },
    { id: 82, name: '玩具' },
    { id: 83, name: '童装' }
  ]},
  { id: 9, name: '运动户外', icon: '⚽', children: [
    { id: 91, name: '运动鞋服' },
    { id: 92, name: '户外装备' }
  ]},
  { id: 10, name: '图书文娱', icon: '📚', children: [
    { id: 101, name: '图书' },
    { id: 102, name: '音像' }
  ]}
]

// 商品图片占位（使用渐变色）
const productColors = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #ff9a9e, #fecfef)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)'
]

// 生成 mock 商品列表
function generateProducts(count = 60) {
  const brands = ['极东自营', '品牌直供', '官方旗舰店', '品质优选']
  const productNames = [
    '智能旗舰手机 5G全网通 12GB+256GB',
    '轻薄笔记本电脑 14英寸 高性能处理器',
    '无线蓝牙耳机 降噪长续航',
    '智能手表 健康监测 运动追踪',
    '4K超高清智能电视 55英寸',
    '变频空调挂机 一级能效 冷暖型',
    '破壁料理机 多功能辅食机',
    '扫地机器人 智能规划路线',
    '纯棉短袖T恤 男款 透气舒适',
    '休闲运动鞋 男女同款 减震',
    '保湿面霜 补水修护 持久滋润',
    '坚果零食大礼包 混合装',
    '积木拼装玩具 益智早教',
    '瑜伽垫 加厚防滑 环保材质',
    '畅销小说文学畅销书',
    '机械键盘 RGB背光 客制化',
    '充电宝 20000mAh 快充',
    '保温杯 316不锈钢 大容量',
    '防晒衣 轻薄透气 UPF50+',
    '智能门锁 指纹密码 刷卡'
  ]
  const products = []
  for (let i = 1; i <= count; i++) {
    const nameIdx = (i - 1) % productNames.length
    const catIdx = (i - 1) % mockCategories.length
    const subCat = mockCategories[catIdx].children[0]
    products.push({
      id: i,
      name: productNames[nameIdx] + ' ' + (i > 20 ? `第${i}款` : ''),
      subtitle: '正品保障 · 极速配送 · 七天无理由退换',
      price: Math.floor(Math.random() * 5000) + 49,
      originalPrice: 0,
      categoryId: mockCategories[catIdx].id,
      categoryName: mockCategories[catIdx].name,
      subCategoryId: subCat.id,
      brand: brands[i % brands.length],
      sales: Math.floor(Math.random() * 10000),
      stock: Math.floor(Math.random() * 500) + 10,
      rating: (Math.random() * 1 + 4).toFixed(1),
      reviewCount: Math.floor(Math.random() * 5000),
      image: productColors[i % productColors.length],
      tags: i % 3 === 0 ? ['自营', '包邮'] : i % 3 === 1 ? ['热销', '限时'] : ['新品', '包邮'],
      merchantId: Math.floor(i / 10) + 1,
      merchantName: brands[i % brands.length]
    })
  }
  return products
}

export const mockProducts = generateProducts(60)

// 商品详情 SKU
function generateSkus(productId) {
  const colors = ['曜石黑', '冰川白', '深海蓝', '樱花粉', '翡翠绿']
  const sizes = ['标准版', '升级版', '尊享版']
  const skus = []
  let skuId = productId * 100
  for (const color of colors.slice(0, 4)) {
    for (const size of sizes.slice(0, 2)) {
      skus.push({
        id: ++skuId,
        productId,
        skuName: `${color} ${size}`,
        price: mockProducts[productId - 1]?.price || 99,
        stock: Math.floor(Math.random() * 200) + 5,
        attributes: { 颜色: color, 版本: size },
        image: productColors[skuId % productColors.length]
      })
    }
  }
  return skus
}

// 商品评价
function generateReviews(productId) {
  const contents = [
    '质量很好，物流也快，非常满意的一次购物体验！',
    '包装精美，产品做工扎实，性价比很高，推荐购买。',
    '用了几天来追评，效果不错，符合预期。',
    '客服态度很好，发货速度快，产品也很满意。',
    '第二次购买了，品质一如既往的好，值得信赖。',
    '外观时尚，功能齐全，价格实惠，五星好评！',
    '物流超快，第二天就到了，产品也很满意。',
    '做工精致，材质不错，用着很舒服，好评。'
  ]
  const users = ['j***8', '匿***户', 't***y', 'l***e', 'w***g', 'a***1', 'm***x', 'c***n']
  const reviews = []
  const count = Math.floor(Math.random() * 5) + 3
  for (let i = 0; i < count; i++) {
    reviews.push({
      id: productId * 1000 + i,
      productId,
      userId: 1000 + i,
      userName: users[i % users.length],
      rating: Math.floor(Math.random() * 2) + 4,
      content: contents[i % contents.length],
      images: [],
      createdAt: new Date(Date.now() - i * 86400000).toISOString().split('T')[0],
      isPurchased: true,
      skuName: '曜石黑 标准版'
    })
  }
  return reviews
}

// 收货地址
export const mockAddresses = [
  {
    id: 1,
    name: '张三',
    phone: '138****8888',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detail: '建国路88号现代城SOHO B座 1801室',
    isDefault: true,
    label: '公司'
  },
  {
    id: 2,
    name: '张三',
    phone: '138****8888',
    province: '北京市',
    city: '北京市',
    district: '海淀区',
    detail: '中关村大街1号院 3号楼 502室',
    isDefault: false,
    label: '家'
  }
]

// 订单状态映射
export const orderStatusMap = {
  'pending_payment': { label: '待付款', color: '#e6a23c', step: 0 },
  'pending_shipment': { label: '待发货', color: '#409eff', step: 1 },
  'shipped': { label: '运输中', color: '#409eff', step: 2 },
  'completed': { label: '已收货', color: '#67c23a', step: 3 },
  'reviewed': { label: '已评价', color: '#909399', step: 4 },
  'cancelled': { label: '已取消', color: '#909399', step: -1 }
}

// 生成 mock 订单
function generateOrders() {
  const statuses = ['pending_payment', 'pending_shipment', 'shipped', 'completed', 'reviewed', 'cancelled']
  const orders = []
  for (let i = 1; i <= 8; i++) {
    const status = statuses[(i - 1) % statuses.length]
    const product = mockProducts[i]
    const items = [{
      id: i * 10,
      orderId: i,
      productId: product.id,
      productName: product.name,
      skuId: product.id * 100 + 1,
      skuName: '曜石黑 标准版',
      quantity: i % 3 + 1,
      unitPrice: product.price,
      image: product.image,
      merchantId: product.merchantId,
      merchantName: product.merchantName
    }]
    const totalAmount = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    orders.push({
      id: i,
      orderNo: `JD${Date.now().toString().slice(-8)}${String(i).padStart(4, '0')}`,
      userId: 1,
      status,
      totalAmount,
      itemCount: items.length,
      items,
      address: mockAddresses[0],
      createdAt: new Date(Date.now() - i * 86400000 * 3).toISOString(),
      payTime: status !== 'pending_payment' && status !== 'cancelled' ? new Date(Date.now() - i * 86400000 * 3 + 3600000).toISOString() : null,
      shipTime: ['shipped', 'completed', 'reviewed'].includes(status) ? new Date(Date.now() - i * 86400000 * 2).toISOString() : null,
      receiveTime: ['completed', 'reviewed'].includes(status) ? new Date(Date.now() - i * 86400000).toISOString() : null,
      logisticsCompany: ['shipped', 'completed', 'reviewed'].includes(status) ? '极东物流' : null,
      logisticsNo: ['shipped', 'completed', 'reviewed'].includes(status) ? `JD${Math.floor(Math.random() * 10000000000)}` : null,
      remark: ''
    })
  }
  return orders
}

export const mockOrders = generateOrders()

// 退款状态映射
export const refundStatusMap = {
  'applied': { label: '已申请', color: '#e6a23c', step: 0, desc: '等待商家审核' },
  'merchant_approved': { label: '商家已同意', color: '#409eff', step: 1, desc: '请尽快寄回商品' },
  'merchant_rejected': { label: '商家已拒绝', color: '#f56c6c', step: 1, desc: '可申请平台介入' },
  'returning': { label: '退货中', color: '#409eff', step: 2, desc: '商品运输中' },
  'refunding': { label: '退款中', color: '#409eff', step: 3, desc: '商家确认收货，退款处理中' },
  'completed': { label: '退款完成', color: '#67c23a', step: 4, desc: '退款已到账' },
  'appealing': { label: '申诉中', color: '#e6a23c', step: 1, desc: '等待平台介入仲裁' },
  'admin_approved': { label: '平台仲裁-同意退款', color: '#67c23a', step: 4, desc: '管理员已介入，同意退款' },
  'admin_rejected': { label: '平台仲裁-拒绝退款', color: '#f56c6c', step: 4, desc: '管理员已介入，拒绝退款' }
}

// 生成 mock 退款
function generateRefunds() {
  const reasons = [
    '商品质量问题',
    '收到商品与描述不符',
    '七天无理由退货',
    '商品损坏/少件',
    '未按约定时间发货',
    '不想要了'
  ]
  const statuses = ['applied', 'merchant_approved', 'returning', 'refunding', 'completed', 'merchant_rejected', 'appealing', 'admin_approved']
  const refunds = []
  for (let i = 1; i <= 5; i++) {
    const status = statuses[(i - 1) % statuses.length]
    const order = mockOrders[i]
    refunds.push({
      id: i,
      orderId: order.id,
      orderNo: order.orderNo,
      userId: 1,
      reason: reasons[i % reasons.length],
      description: '收到商品后发现与预期不符，希望退款处理。',
      status,
      amount: order.totalAmount,
      merchantId: order.items[0].merchantId,
      merchantName: order.items[0].merchantName,
      items: order.items,
      appliedAt: new Date(Date.now() - i * 86400000 * 2).toISOString(),
      merchantAuditTime: ['merchant_approved', 'returning', 'refunding', 'completed', 'merchant_rejected'].includes(status) ? new Date(Date.now() - i * 86400000).toISOString() : null,
      merchantAuditRemark: ['merchant_approved', 'returning', 'refunding', 'completed'].includes(status) ? '同意退款，请寄回商品' : status === 'merchant_rejected' ? '商品无质量问题，拒绝退款' : null,
      returnLogisticsCompany: ['returning', 'refunding', 'completed'].includes(status) ? '顺丰速运' : null,
      returnLogisticsNo: ['returning', 'refunding', 'completed'].includes(status) ? `SF${Math.floor(Math.random() * 10000000000)}` : null,
      refundTime: ['completed'].includes(status) ? new Date(Date.now() - i * 86400000).toISOString() : null,
      appealTime: ['appealing', 'admin_approved'].includes(status) ? new Date(Date.now() - 86400000).toISOString() : null,
      appealReason: ['appealing', 'admin_approved'].includes(status) ? '商家超时未处理，申请平台介入' : null,
      adminHandleTime: ['admin_approved'].includes(status) ? new Date().toISOString() : null,
      adminRemark: ['admin_approved'].includes(status) ? '经核实，同意用户退款申请' : null,
      isOvertime: status === 'applied' && i === 1 // 第一条模拟超时
    })
  }
  return refunds
}

export const mockRefunds = generateRefunds()

// 轮播图
export const mockBanners = [
  { id: 1, title: '年终大促', subtitle: '全场低至5折', image: 'linear-gradient(135deg, #e1251b, #f5af19)', link: '/product-list' },
  { id: 2, title: '新品首发', subtitle: '科技引领未来', image: 'linear-gradient(135deg, #2193b0, #6dd5ed)', link: '/product-list' },
  { id: 3, title: '品质生活', subtitle: '精选好物推荐', image: 'linear-gradient(135deg, #834d9b, #d04ed6)', link: '/product-list' },
  { id: 4, title: '会员专享', subtitle: '专属优惠等你来', image: 'linear-gradient(135deg, #393E46, #00ADB9)', link: '/product-list' }
]

// 导出辅助函数
export function getProductSkus(productId) {
  return generateSkus(Number(productId))
}

export function getProductReviews(productId) {
  return generateReviews(Number(productId))
}

// 用户信息
export const mockUserInfo = {
  id: 1,
  username: 'jd_user',
  nickname: '极东用户',
  phone: '138****8888',
  email: 'user@example.com',
  avatar: 'linear-gradient(135deg, #e1251b, #f5af19)',
  level: 'PLUS会员',
  points: 2680,
  couponCount: 5,
  balance: 128.50
}
