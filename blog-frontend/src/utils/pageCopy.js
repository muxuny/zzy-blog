export const PAGE_COPY_GROUPS = ['公开与用户页', '创作中心', '后台管理']

export const PAGE_COPY_DEFAULTS = [
  {
    copyKey: 'home.hero',
    pageName: '首页',
    pageGroup: '公开与用户页',
    eyebrow: '个人写作库',
    title: '把项目经验写成可以回看的路标。',
    description: '这里记录开发实践、阅读笔记和阶段性思考。文章不追求热闹，更关心一个问题从出现到解决的过程。',
    sortOrder: 10
  },
  {
    copyKey: 'tag.index',
    pageName: '标签页',
    pageGroup: '公开与用户页',
    eyebrow: '话题索引',
    title: '{tagName}',
    description: '按标签收束后的文章流，只保留同一话题下的公开记录。',
    sortOrder: 20
  },
  {
    copyKey: 'auth.login',
    pageName: '登录',
    pageGroup: '公开与用户页',
    eyebrow: '登录后继续',
    title: '接上刚才的阅读和创作。',
    description: '登录后会优先回到你刚才要打开的页面；没有指定入口时，会进入首页，管理员可直接进入后台。',
    sortOrder: 30
  },
  {
    copyKey: 'auth.register',
    pageName: '注册',
    pageGroup: '公开与用户页',
    eyebrow: '新入口',
    title: '给自己的内容留一个稳定身份。',
    description: '注册后等待审核，通过后就可以继续阅读、写文章和进入对应的管理入口。',
    sortOrder: 40
  },
  {
    copyKey: 'reading.overview',
    pageName: '我的阅读',
    pageGroup: '公开与用户页',
    eyebrow: 'Reading desk',
    title: '我的阅读更像一个安静的续接台。',
    description: '这里不负责发现热门内容，只负责把读者和自己的阅读轨迹接起来。轻微动态集中在继续阅读和历史焦点上。',
    sortOrder: 50
  },
  {
    copyKey: 'reading.history',
    pageName: '阅读历史',
    pageGroup: '公开与用户页',
    eyebrow: 'Reading trail',
    title: '阅读历史',
    description: '按时间回看读过的文章，保留标题快照，也允许清理不再需要的单条记录。',
    sortOrder: 60
  },
  {
    copyKey: 'favorites.index',
    pageName: '我的收藏',
    pageGroup: '公开与用户页',
    eyebrow: 'Pinned index',
    title: '我的收藏',
    description: '把值得回看的文章收进一个轻量索引，筛选仍然服务于快速返回内容本身。',
    sortOrder: 70
  },
  {
    copyKey: 'creator.articles',
    pageName: '创作文章',
    pageGroup: '创作中心',
    eyebrow: 'Creator console',
    title: '创作空间保留控制台感，但触感更轻。',
    description: '创作者最需要效率，所以动态只用于聚焦当前行、状态筛选和预览反馈。它应该让后台工作更顺手，而不是更花。',
    sortOrder: 110
  },
  {
    copyKey: 'creator.article.create',
    pageName: '创作写文章',
    pageGroup: '创作中心',
    eyebrow: '创作中心',
    title: '写文章',
    description: '把标题、摘要、分组和正文放在同一个工作流里，状态动作保持在明确的底部区域。',
    sortOrder: 120
  },
  {
    copyKey: 'creator.article.edit',
    pageName: '创作编辑文章',
    pageGroup: '创作中心',
    eyebrow: '创作中心',
    title: '编辑文章',
    description: '把标题、摘要、分组和正文放在同一个工作流里，状态动作保持在明确的底部区域。',
    sortOrder: 130
  },
  {
    copyKey: 'admin.dashboard',
    pageName: '后台仪表盘',
    pageGroup: '后台管理',
    eyebrow: '概览',
    title: '仪表盘',
    description: '全站内容状态、审核队列和资源概览在这里集中查看。',
    sortOrder: 210
  },
  {
    copyKey: 'admin.articles',
    pageName: '后台文章管理',
    pageGroup: '后台管理',
    eyebrow: '内容审核',
    title: '文章管理',
    description: '审核、筛选和维护全站文章，确保公开内容状态清晰。',
    sortOrder: 220
  },
  {
    copyKey: 'admin.article.create',
    pageName: '后台写文章',
    pageGroup: '后台管理',
    eyebrow: '内容创建',
    title: '写文章',
    description: '以管理员身份创建文章，并直接处理发布状态。',
    sortOrder: 230
  },
  {
    copyKey: 'admin.article.edit',
    pageName: '后台编辑文章',
    pageGroup: '后台管理',
    eyebrow: '内容编辑',
    title: '编辑文章',
    description: '维护文章正文、标签、可见性和审核状态。',
    sortOrder: 240
  },
  {
    copyKey: 'admin.resources',
    pageName: '资源管理',
    pageGroup: '后台管理',
    eyebrow: '内容资源',
    title: '资源管理',
    description: '统一维护标签库和图片素材，支撑公开筛选与文章编辑。',
    sortOrder: 250
  },
  {
    copyKey: 'admin.users',
    pageName: '用户管理',
    pageGroup: '后台管理',
    eyebrow: '账号审核',
    title: '用户管理',
    description: '处理账号审核和禁用状态，控制创作与后台入口权限。',
    sortOrder: 260
  },
  {
    copyKey: 'admin.profile',
    pageName: '个人资料',
    pageGroup: '后台管理',
    eyebrow: '个人资料',
    title: '个人资料',
    description: '查看当前管理员账号信息和后台访问状态。',
    sortOrder: 270
  }
]

const DEFAULT_COPY_MAP = new Map(PAGE_COPY_DEFAULTS.map(item => [item.copyKey, item]))

const FALLBACK_COPY = {
  copyKey: '',
  pageName: '页面',
  pageGroup: '未分组',
  eyebrow: '页面',
  title: '页面',
  description: '',
  sortOrder: 0
}

export function mergePageCopies(remoteCopies = []) {
  const remoteMap = new Map()
  if (Array.isArray(remoteCopies)) {
    remoteCopies.forEach(item => {
      if (item?.copyKey && DEFAULT_COPY_MAP.has(item.copyKey)) {
        remoteMap.set(item.copyKey, item)
      }
    })
  }

  return PAGE_COPY_DEFAULTS.map(defaultCopy => normalizePageCopy({
    ...defaultCopy,
    ...pickEditableFields(remoteMap.get(defaultCopy.copyKey))
  }, defaultCopy))
}

export function resolvePageCopy(copyKey, params = {}, copies = PAGE_COPY_DEFAULTS) {
  const source = Array.isArray(copies) ? copies : PAGE_COPY_DEFAULTS
  const base = source.find(item => item.copyKey === copyKey)
    || DEFAULT_COPY_MAP.get(copyKey)
    || { ...FALLBACK_COPY, copyKey }
  const fallback = DEFAULT_COPY_MAP.get(copyKey) || FALLBACK_COPY
  const copy = normalizePageCopy(base, { ...fallback, copyKey })

  return {
    ...copy,
    eyebrow: interpolateCopyText(copy.eyebrow, params),
    title: interpolateCopyText(copy.title, params),
    description: interpolateCopyText(copy.description, params)
  }
}

export function groupPageCopies(copies = PAGE_COPY_DEFAULTS) {
  return PAGE_COPY_GROUPS.map(group => ({
    group,
    items: copies.filter(item => item.pageGroup === group)
  }))
}

function pickEditableFields(item) {
  if (!item) return {}
  return {
    eyebrow: typeof item.eyebrow === 'string' ? item.eyebrow : undefined,
    title: typeof item.title === 'string' ? item.title : undefined,
    description: typeof item.description === 'string' ? item.description : undefined
  }
}

function normalizePageCopy(item, fallback) {
  const title = normalizeOptionalText(item.title)
  return {
    copyKey: normalizeOptionalText(item.copyKey) || fallback.copyKey,
    pageName: normalizeOptionalText(item.pageName) || fallback.pageName,
    pageGroup: normalizeOptionalText(item.pageGroup) || fallback.pageGroup,
    eyebrow: normalizeNullableText(item.eyebrow, fallback.eyebrow),
    title: title || fallback.title,
    description: normalizeNullableText(item.description, fallback.description),
    sortOrder: Number.isFinite(Number(item.sortOrder)) ? Number(item.sortOrder) : fallback.sortOrder
  }
}

function normalizeOptionalText(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function normalizeNullableText(value, fallback) {
  return typeof value === 'string' ? value.trim() : fallback
}

function interpolateCopyText(text, params) {
  const tagName = normalizeOptionalText(params?.tagName) || '标签'
  return String(text || '').replace(/\{tagName\}/g, tagName)
}
