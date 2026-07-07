/** 管理员后台 Mock 数据 */

export const mockDashboard = {
  stats: {
    totalSales: 1286432.50,
    totalOrders: 8654,
    totalUsers: 23456,
    totalMerchants: 128,
    todaySales: 32567.80,
    todayOrders: 234,
    todayUsers: 56,
    pendingRefunds: 8,
    pendingAudits: 12
  },
  salesChart: {
    dates: ['7/1','7/2','7/3','7/4','7/5','7/6','7/7'],
    values: [125000, 132000, 118000, 145000, 138000, 156000, 142000]
  },
  orderChart: {
    statuses: ['待付款','待发货','已发货','已完成','已取消'],
    values: [120, 340, 560, 7800, 434]
  },
  categoryChart: {
    names: ['手机数码','电脑办公','家用电器','服饰内衣','美妆护肤','食品生鲜'],
    values: [3200, 2800, 1500, 1200, 980, 760]
  }
}

export const mockUsers = Array.from({length: 20}, (_, i) => ({
  id: i + 1,
  username: `user_${String(i+1).padStart(4,'0')}`,
  nickname: `用户${i+1}`,
  phone: `138${String(80000000 + i).slice(-8)}`,
  email: `user${i+1}@example.com`,
  status: i % 5 === 0 ? 'disabled' : 'active',
  role: i === 0 ? 'admin' : 'user',
  createdAt: `2024-0${(i%9)+1}-15 10:30:00`,
  orderCount: Math.floor(Math.random() * 50),
  totalSpent: Math.floor(Math.random() * 10000)
}))

export const mockMerchants = Array.from({length: 10}, (_, i) => ({
  id: i + 1,
  userId: i + 1,
  shopName: ['极东自营旗舰店','数码专营店','家电官方店','服饰潮流馆','美妆优选铺','生鲜直供店','母婴安心购','运动达人馆','图书文轩阁','品质生活馆'][i],
  status: i < 7 ? 'approved' : i === 7 ? 'pending' : i === 8 ? 'rejected' : 'approved',
  auditStatus: i < 7 ? 'approved' : i === 7 ? 'pending' : 'rejected',
  contactName: `联系人${i+1}`,
  contactPhone: `139${String(80000000 + i).slice(-8)}`,
  productCount: Math.floor(Math.random() * 100) + 10,
  orderCount: Math.floor(Math.random() * 500),
  createdAt: `2024-0${(i%9)+1}-10 14:00:00`
}))

export const mockProductsAudit = Array.from({length: 15}, (_, i) => ({
  id: i + 1,
  name: ['智能旗舰手机','轻薄笔记本','无线耳机','智能手表','4K电视','变频空调','破壁机','扫地机器人','纯棉T恤','运动鞋','保湿面霜','坚果礼包','积木玩具','瑜伽垫','畅销小说'][i],
  merchantName: mockMerchants[i % 10].shopName,
  categoryId: (i % 10) + 1,
  price: Math.floor(Math.random() * 3000) + 99,
  auditStatus: i < 5 ? 'pending' : i < 10 ? 'approved' : 'rejected',
  submittedAt: `2024-07-0${(i%7)+1} 09:00:00`,
  description: '商品描述信息...'
}))

export const mockAdminOrders = Array.from({length: 20}, (_, i) => ({
  id: i + 1,
  orderNo: `JD2024070${String(i+1).padStart(5,'0')}`,
  userId: (i % 20) + 1,
  userName: mockUsers[i % 20].nickname,
  merchantId: (i % 10) + 1,
  merchantName: mockMerchants[i % 10].shopName,
  totalAmount: Math.floor(Math.random() * 2000) + 50,
  status: ['pending_payment','pending_shipment','shipped','completed','cancelled'][i % 5],
  itemCount: (i % 3) + 1,
  createdAt: `2024-07-0${(i%7)+1} ${String(10+i).padStart(2,'0')}:30:00`
}))

export const mockRefundsArbitration = Array.from({length: 8}, (_, i) => ({
  id: i + 1,
  orderId: i + 1,
  orderNo: `JD2024070${String(i+1).padStart(5,'0')}`,
  userId: (i % 20) + 1,
  userName: mockUsers[i % 20].nickname,
  merchantId: (i % 10) + 1,
  merchantName: mockMerchants[i % 10].shopName,
  reason: ['商品质量问题','与描述不符','七天无理由','商品损坏','未按时发货'][i % 5],
  amount: Math.floor(Math.random() * 1000) + 50,
  status: ['appealing','appealing','admin_approved','admin_rejected','appealing','appealing','admin_approved','appealing'][i],
  appliedAt: `2024-07-0${(i%7)+1} 10:00:00`,
  merchantAuditTime: i % 3 === 0 ? null : `2024-07-0${(i%7)+1} 14:00:00`,
  merchantAuditResult: i % 3 === 0 ? null : i % 2 === 0 ? 'approved' : 'rejected',
  merchantAuditRemark: i % 3 === 0 ? null : i % 2 === 0 ? '同意退款' : '拒绝退款',
  appealTime: ['appealing','admin_approved','admin_rejected'].includes(['appealing','appealing','admin_approved','admin_rejected','appealing','appealing','admin_approved','appealing'][i]) ? `2024-07-0${(i%7)+1} 16:00:00` : null,
  appealReason: '商家超时未处理/对结果不满意',
  adminHandleTime: ['admin_approved','admin_rejected'].includes(['appealing','appealing','admin_approved','admin_rejected','appealing','appealing','admin_approved','appealing'][i]) ? `2024-07-0${(i%7)+1} 18:00:00` : null,
  adminResult: ['admin_approved','admin_rejected'].includes(['appealing','appealing','admin_approved','admin_rejected','appealing','appealing','admin_approved','appealing'][i]) ? (i === 2 || i === 6 ? 'approved' : 'rejected') : null,
  adminRemark: i === 2 ? '经核实同意退款' : i === 3 ? '证据不足拒绝' : i === 6 ? '商家责任同意退款' : null,
  isUrgent: i % 3 === 0
}))

export const mockAdminLogs = Array.from({length: 20}, (_, i) => ({
  id: i + 1,
  adminId: 1,
  adminName: '超级管理员',
  action: ['用户禁用','商家审核通过','商品审核拒绝','退款仲裁','商家审核拒绝','用户启用'][i % 6],
  targetId: i + 1,
  targetName: `目标对象${i+1}`,
  detail: '操作详情描述...',
  createdAt: `2024-07-0${(i%7)+1} ${String(10+i).padStart(2,'0')}:${String(i*3).padStart(2,'0')}:00`
}))

export const mockAdminInfo = {
  id: 1,
  username: 'admin',
  nickname: '超级管理员',
  role: 'super_admin',
  avatar: 'linear-gradient(135deg, #1890ff, #36cfc9)'
}
