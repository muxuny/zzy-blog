<template>
  <el-popover
    popper-class="appearance-popover"
    placement="bottom-end"
    trigger="click"
    width="320"
  >
    <template #reference>
      <button type="button" class="appearance-trigger" aria-label="打开外观设置">
        <span class="appearance-dot" aria-hidden="true" />
        <span>外观</span>
      </button>
    </template>

    <section class="appearance-section" aria-label="明暗模式">
      <span class="appearance-label">明暗模式</span>
      <div class="mode-grid">
        <button
          v-for="mode in themeStore.modes"
          :key="mode.value"
          type="button"
          class="mode-option"
          :class="{ 'is-active': themeStore.mode === mode.value }"
          :aria-pressed="themeStore.mode === mode.value"
          @click="themeStore.setMode(mode.value)"
        >
          {{ mode.label }}
        </button>
      </div>
    </section>

    <section class="appearance-section" aria-label="固定配色">
      <span class="appearance-label">固定配色</span>
      <div class="palette-list">
        <button
          v-for="palette in themeStore.palettes"
          :key="palette.value"
          type="button"
          class="palette-option"
          :class="{ 'is-active': themeStore.palette === palette.value }"
          :aria-pressed="themeStore.palette === palette.value"
          @click="themeStore.setPalette(palette.value)"
        >
          <span class="palette-swatch" :data-palette-swatch="palette.value" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
          <span class="palette-copy">
            <strong>{{ palette.label }}</strong>
            <small>{{ palette.description }}</small>
          </span>
        </button>
      </div>
    </section>
  </el-popover>
</template>

<script setup>
import { useThemeStore } from '../stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.appearance-trigger {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  min-height: 36px;
  padding: 0 12px;
  border: 1px solid var(--soft-border-color);
  border-radius: 999px;
  background: color-mix(in srgb, var(--panel-bg) 86%, transparent);
  color: var(--muted-text-color);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: border-color 0.18s ease, background-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.appearance-trigger:hover {
  border-color: color-mix(in srgb, var(--primary-color) 34%, var(--soft-border-color));
  background: var(--panel-bg);
  color: var(--text-color);
  transform: translateY(-1px);
}

.appearance-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color), var(--warning-color));
  box-shadow: 0 0 0 4px var(--primary-ring-color);
}

.appearance-section + .appearance-section {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--soft-border-color);
}

.appearance-label {
  display: block;
  margin-bottom: 8px;
  color: var(--muted-text-color);
  font-size: 12px;
  font-weight: 750;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.mode-option,
.palette-option {
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-sm);
  background: var(--panel-bg);
  color: var(--muted-text-color);
  font: inherit;
  cursor: pointer;
}

.mode-option {
  min-height: 34px;
}

.mode-option.is-active,
.palette-option.is-active {
  border-color: color-mix(in srgb, var(--primary-color) 42%, var(--soft-border-color));
  background: color-mix(in srgb, var(--primary-color) 9%, var(--panel-bg));
  color: var(--text-color);
}

.palette-list {
  display: grid;
  gap: 8px;
}

.palette-option {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-height: 52px;
  padding: 8px 10px;
  text-align: left;
}

.palette-swatch {
  display: flex;
  gap: 4px;
}

.palette-swatch i {
  width: 14px;
  height: 30px;
  border-radius: 999px;
  background: var(--primary-color);
}

.palette-swatch i:nth-child(2) {
  background: var(--accent-color);
}

.palette-swatch i:nth-child(3) {
  background: var(--warning-color);
}

.palette-copy strong,
.palette-copy small {
  display: block;
}

.palette-copy small {
  margin-top: 2px;
  color: var(--muted-text-color);
  font-size: 12px;
}

@media (max-width: 640px) {
  .appearance-trigger span:last-child {
    display: none;
  }
}
</style>
