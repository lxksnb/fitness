<template>
  <div class="muscle-map">
    <svg viewBox="0 0 360 260" role="img" aria-label="人体肌群高亮图">
      <g class="figure">
        <text x="90" y="20">正面</text>
        <circle cx="90" cy="42" r="18" />
        <rect x="68" y="65" width="44" height="76" rx="20" :class="muscleClass('CHEST_MIDDLE')" />
        <path d="M70 66 Q90 48 110 66 L104 86 Q90 78 76 86 Z" :class="muscleClass('CHEST_UPPER')" />
        <path d="M74 124 Q90 136 106 124 L104 144 Q90 154 76 144 Z" :class="muscleClass('CHEST_LOWER')" />
        <path d="M60 70 Q44 78 40 105 L54 108 Q58 90 72 78 Z" :class="muscleClass('FRONT_DELTS')" />
        <path d="M120 70 Q136 78 140 105 L126 108 Q122 90 108 78 Z" :class="muscleClass('FRONT_DELTS')" />
        <path d="M38 108 L54 108 L52 156 L36 156 Z" :class="muscleClass('BICEPS')" />
        <path d="M126 108 L142 108 L144 156 L128 156 Z" :class="muscleClass('BICEPS')" />
        <path d="M36 158 L52 158 L50 210 L34 210 Z" :class="muscleClass('FOREARMS')" />
        <path d="M128 158 L144 158 L146 210 L130 210 Z" :class="muscleClass('FOREARMS')" />
        <path d="M72 146 L88 146 L86 220 L64 220 Z" :class="muscleClass('QUADS')" />
        <path d="M92 146 L108 146 L116 220 L94 220 Z" :class="muscleClass('QUADS')" />
        <path d="M66 220 L86 220 L82 250 L62 250 Z" :class="muscleClass('CALVES')" />
        <path d="M96 220 L116 220 L120 250 L100 250 Z" :class="muscleClass('CALVES')" />
        <rect x="78" y="86" width="24" height="58" rx="8" :class="muscleClass('ABS')" />
        <path d="M68 90 L78 90 L76 142 L66 142 Z" :class="muscleClass('OBLIQUES')" />
        <path d="M102 90 L112 90 L114 142 L104 142 Z" :class="muscleClass('OBLIQUES')" />

        <text x="250" y="20">背面</text>
        <circle cx="250" cy="42" r="18" />
        <path d="M224 68 L276 68 L264 144 L236 144 Z" :class="muscleClass('LATS')" />
        <path d="M230 66 Q250 50 270 66 L264 92 L236 92 Z" :class="muscleClass('UPPER_BACK')" />
        <path d="M238 58 L262 58 L268 78 L232 78 Z" :class="muscleClass('TRAPS')" />
        <path d="M216 70 Q200 78 196 105 L210 108 Q214 90 228 78 Z" :class="muscleClass('REAR_DELTS')" />
        <path d="M284 70 Q300 78 304 105 L290 108 Q286 90 272 78 Z" :class="muscleClass('REAR_DELTS')" />
        <path d="M194 108 L210 108 L208 156 L192 156 Z" :class="muscleClass('TRICEPS')" />
        <path d="M290 108 L306 108 L308 156 L292 156 Z" :class="muscleClass('TRICEPS')" />
        <path d="M236 126 L264 126 L266 154 L234 154 Z" :class="muscleClass('LOWER_BACK')" />
        <path d="M230 146 L250 146 L246 190 L224 190 Z" :class="muscleClass('GLUTES')" />
        <path d="M250 146 L270 146 L276 190 L254 190 Z" :class="muscleClass('GLUTES')" />
        <path d="M224 190 L246 190 L242 220 L222 220 Z" :class="muscleClass('HAMSTRINGS')" />
        <path d="M254 190 L276 190 L278 220 L258 220 Z" :class="muscleClass('HAMSTRINGS')" />
      </g>
    </svg>
    <div class="legend">
      <span><i class="primary"></i>主要刺激</span>
      <span><i class="secondary"></i>辅助刺激</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  primaryMuscles?: string[]
  secondaryMuscles?: string[]
}>()

function muscleClass(code: string) {
  return {
    primary: props.primaryMuscles?.includes(code),
    secondary: props.secondaryMuscles?.includes(code)
  }
}
</script>

<style scoped lang="scss">
.muscle-map {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px;
  background: #fbfcfe;
}

svg {
  width: 100%;
  height: 220px;
  display: block;
}

.figure {
  text {
    fill: #606266;
    font-size: 14px;
    text-anchor: middle;
  }

  circle,
  rect,
  path {
    fill: #eef1f6;
    stroke: #cfd5df;
    stroke-width: 1.5;
  }

  .primary {
    fill: #f56c6c;
    stroke: #c45656;
  }

  .secondary {
    fill: #e6a23c;
    stroke: #b88230;
  }
}

.legend {
  display: flex;
  justify-content: center;
  gap: 18px;
  font-size: 12px;
  color: #606266;

  span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  i {
    width: 10px;
    height: 10px;
    border-radius: 2px;
    display: inline-block;
  }

  .primary {
    background: #f56c6c;
  }

  .secondary {
    background: #e6a23c;
  }
}
</style>
