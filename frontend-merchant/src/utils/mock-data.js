import { pickProductImage } from '@/utils/product-images'

/** 商家后台 Mock 数据 */

export const mockMerchantInfo = {
  id: 1,
  shopName: '极东数码旗舰店',
  contactName: '张经理',
  contactPhone: '13912345678',
  contactEmail: 'shop@jidong.com',
  description: '专营数码电子产品，正品保障，全国联保。',
  logo: 'linear-gradient(135deg, #ff6a00, #ff9a44)',
  status: 'approved',
  createdAt: '2024-01-15 10:00:00'
}

export const mockMerchantDashboard = {
  stats: {
    todaySales: 12580.50,
    todayOrders: 34,
    totalSales: 456789.00,
    totalOrders: 1234,
    pendingShipment: 12,
    pendingRefunds: 3,
    productCount: 86,
    lowStockCount: 5
  },
  salesChart: {
    dates: ['7/1','7/2','7/3','7/4','7/5','7/6','7/7'],
    values: [8200, 9500, 7800, 11200, 10600, 12580, 9800]
  },
  orderChart: {
    statuses: ['待付款','待发货','已发货','已完成','已取消'],
    values: [8, 12, 23, 180, 11]
  },
  topProducts: [
    { name: '智能旗舰手机', sales: 156, amount: 780000 },
    { name: '无线降噪耳机', sales: 89, amount: 178000 },
    { name: '轻薄笔记本', sales: 45, amount: 225000 },
    { name: '智能手表', sales: 67, amount: 67000 },
    { name: '快充移动电源', sales: 120, amount: 36000 }
  ]
}

export const mockMerchantProducts = Array.from({ length: 15 }, (_, i) => ({
  id: i + 1,
  name: ['智能旗舰手机','轻薄笔记本','无线降噪耳机','智能手表','4K显示器','机械键盘','无线鼠标','USB扩展坞','快充移动电源','蓝牙音箱','智能门锁','扫地机器人','电动牙刷','空气炸锅','投影仪'][i],
  categoryId: (i % 6) + 1,
  price: [4999, 5999, 899, 1299, 2199, 399, 159, 199, 299, 499, 1599, 1899, 299, 399, 2599][i],
  stock: [120, 80, 200, 150, 60, 300, 500, 400, 600, 250, 40, 35, 800, 700, 30][i],
  sales: [156, 45, 89, 67, 23, 120, 200, 150, 120, 80, 12, 34, 300, 250, 15][i],
  status: i < 12 ? 'on_sale' : i === 12 ? 'off_shelf' : i === 13 ? 'draft' : 'under_review',
  mainImage: pickProductImage(i),
  createdAt: `2024-0${(i % 9) + 1}-10 10:00:00`,
  skus: [
    { id: i * 10 + 1, name: '标准版', price: [4999, 5999, 899, 1299, 2199, 399, 159, 199, 299, 499, 1599, 1899, 299, 399, 2599][i], stock: 100 },
    { id: i * 10 + 2, name: '高配版', price: [4999, 5999, 899, 1299, 2199, 399, 159, 199, 299, 499, 1599, 1899, 299, 399, 2599][i] + 500, stock: 50 }
  ]
}))

export const mockMerchantOrders = Array.from({ length: 20 }, (_, i) => ({
  id: i + 1,
  orderNo: `MO${String(i + 1).padStart(6, '0')}`,
  userName: `买家${i + 1}`,
  totalAmount: Math.floor(Math.random() * 3000) + 99,
  status: ['pending_payment', 'pending_shipment', 'shipped', 'completed', 'cancelled'][i % 5],
  itemCount: (i % 3) + 1,
  shippingAddress: `北京市朝阳区xx路${i + 1}号`,
  logisticsCompany: i % 2 === 0 ? '顺丰速运' : '中通快递',
  trackingNo: i > 4 ? `SF${String(1000000 + i).padStart(10, '0')}` : '',
  createdAt: `2024-07-0${(i % 7) + 1} ${String(10 + i).padStart(2, '0')}:30:00`,
  items: [
    { productName: mockMerchantProducts[i % 15].name, skuName: '标准版', quantity: (i % 3) + 1, unitPrice: mockMerchantProducts[i % 15].price }
  ]
}))

export const mockMerchantRefunds = Array.from({ length: 8 }, (_, i) => ({
  id: i + 1,
  orderId: i + 1,
  orderNo: `MO${String(i + 1).padStart(6, '0')}`,
  userName: `买家${i + 1}`,
  productName: mockMerchantProducts[i % 15].name,
  reason: ['商品质量问题', '与描述不符', '七天无理由', '商品损坏', '不喜欢', '尺寸不符', '发错货', '其他'][i],
  amount: Math.floor(Math.random() * 1000) + 50,
  status: ['pending', 'pending', 'approved', 'rejected', 'pending', 'pending', 'approved', 'pending'][i],
  appliedAt: `2024-07-0${(i % 7) + 1} 10:00:00`,
  isUrgent: i % 3 === 0,
  deadline: `2024-07-0${(i % 7) + 1} 22:00:00`,
  userRemark: '请尽快处理',
  merchantRemark: i >= 2 ? '已联系买家确认' : ''
}))

export const mockMerchantReviews = Array.from({ length: 10 }, (_, i) => ({
  id: i + 1,
  productName: mockMerchantProducts[i % 15].name,
  userName: `买家${i + 1}`,
  rating: [5, 4, 5, 3, 5, 4, 2, 5, 4, 5][i],
  content: ['质量很好，物流也快！', '包装精致，产品不错', '性价比超高，推荐！', '一般般吧', '正品保障，放心购买', '用了几天，体验不错', '有点小瑕疵', '非常满意！', '还行', '第二次购买了，一如既往好'][i],
  images: i % 3 === 0 ? [pickProductImage(i), pickProductImage(i + 1)] : [],
  createdAt: `2024-07-0${(i % 7) + 1} 15:00:00`,
  reply: i < 4 ? '感谢您的支持！' : ''
}))

export const mockMerchantConfigs = {
  freeShippingThreshold: 99,
  defaultShippingFee: 10,
  returnAddress: '北京市朝阳区仓储中心',
  autoReplyMessage: '亲，欢迎光临！如有问题请随时联系客服~',
  businessHours: '周一至周日 09:00-22:00'
}
