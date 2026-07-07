<template>
  <footer class="app-footer">
    <!-- 服务保障 -->
    <div class="service-bar">
      <div class="container service-inner">
        <div v-for="item in services" :key="item.title" class="service-item">
          <span class="service-icon">{{ item.icon }}</span>
          <div class="service-text">
            <h4>{{ item.title }}</h4>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部链接 -->
    <div class="footer-links">
      <div class="container">
        <div class="links-grid">
          <div v-for="group in linkGroups" :key="group.title" class="link-group">
            <h4>{{ group.title }}</h4>
            <ul>
              <li v-for="link in group.links" :key="link">
                <a href="javascript:void(0)">{{ link }}</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 版权 -->
    <div class="copyright">
      <div class="container">
        <p>© 2024-2026 极东商城 JD-Mall.com 版权所有 | 京ICP证000000号 | 本站为演示项目，仅供学习交流</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
const services = [
  { icon: '✅', title: '正品保障', desc: '正品保障 假一赔十' },
  { icon: '🚚', title: '极速配送', desc: '当日达 次日达' },
  { icon: '↩️', title: '七天退换', desc: '七天无理由退换货' },
  { icon: '🎧', title: '24h客服', desc: '全天候在线服务' }
]

const linkGroups = [
  { title: '购物指南', links: ['购物流程', '会员介绍', '生活旅行', '常见问题', '大家电'] },
  { title: '配送方式', links: ['上门自提', '211限时达', '配送服务查询', '配送费收取标准'] },
  { title: '支付方式', links: ['货到付款', '在线支付', '分期付款', '邮局汇款', '公司转账'] },
  { title: '售后服务', links: ['售后政策', '价格保护', '退款说明', '返修退换货', '取消订单'] },
  { title: '特色服务', links: ['夺宝岛', 'DIY装机', '延保服务', '企业采购', '校园频道'] }
]
</script>

<style scoped>
.app-footer {
  background: #fff;
  border-top: 1px solid var(--border-light);
  margin-top: 40px;
}

/* 服务保障 */
.service-bar {
  border-bottom: 1px solid var(--border-light);
  padding: 24px 0;
}

.service-inner {
  display: flex;
  justify-content: space-around;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-icon {
  font-size: 32px;
}

.service-text h4 {
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.service-text p {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 底部链接 */
.footer-links {
  padding: 32px 0;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}

.link-group h4 {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.link-group ul li {
  margin-bottom: 8px;
}

.link-group a {
  font-size: 12px;
  color: var(--text-secondary);
}

.link-group a:hover {
  color: var(--jd-red);
}

/* 版权 */
.copyright {
  border-top: 1px solid var(--border-light);
  padding: 20px 0;
  text-align: center;
}

.copyright p {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
