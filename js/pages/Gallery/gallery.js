/**
 * Canvas 图片画廊（二维无限滚动 + 每列独立状态）
 */
import { getGalleryCanvas } from "../../dom/galleryDom.js";

// ================= 配置 =================
const config = {
  imgWidth: 300,
  imgHeight: 450,
  minColumnGapX: 80,
  baseGapY: 50,
  columnShiftX: 18,
  cornerRadius: 16,
};

// ================= 状态 =================
const state = {
  offsetX: 0,
  offsetY: 0,
  isDragging: false,
  lastMouse: { x: 0, y: 0 },
  images: [],
};

// ================= DOM =================
const canvas = getGalleryCanvas();
const ctx = canvas.getContext("2d");

// ================= 图片缓存 =================
const imageCache = new Map();

function initImages() {
  if (state.images.length > 0) {
    return;
  }

  state.images = Array.from(
    { length: 200 },
    (_, i) => `https://picsum.photos/seed/${i}/600/800`,
  );
}

// 加载图片（只创建一次）
function loadImage(src) {
  if (imageCache.has(src)) {
    return imageCache.get(src);
  }

  const img = new Image();
  img.src = src;

  imageCache.set(src, img);
  return img;
}

// ================= 裁剪绘制（类似 object-fit: cover） =================
function drawImageCover(ctx, img, x, y, w, h) {
  const imgRatio = img.width / img.height;
  const boxRatio = w / h;

  let sx;
  let sy;
  let sw;
  let sh;

  if (imgRatio > boxRatio) {
    sh = img.height;
    sw = sh * boxRatio;
    sx = (img.width - sw) / 2;
    sy = 0;
  } else {
    sw = img.width;
    sh = sw / boxRatio;
    sx = 0;
    sy = (img.height - sh) / 2;
  }

  ctx.drawImage(img, sx, sy, sw, sh, x, y, w, h);
}

// ================= 列布局 =================
function getColumnLayout(viewportWidth) {
  const columnCount = Math.max(
    2,
    Math.min(
      5,
      Math.floor(
        (viewportWidth + config.minColumnGapX) /
          (config.imgWidth + config.minColumnGapX),
      ),
    ),
  );

  const spacingX = Math.max(
    config.minColumnGapX,
    Math.floor(
      (viewportWidth - columnCount * config.imgWidth) / (columnCount + 1),
    ),
  );

  const repeatWidth = columnCount * (config.imgWidth + spacingX);
  const startX = Math.max(0, (viewportWidth - repeatWidth + spacingX) / 2);

  return {
    columnCount,
    spacingX,
    repeatWidth,
    startX,
  };
}

function getVisibleBandRange(offsetX, viewportWidth, layout) {
  const buffer = config.imgWidth + layout.spacingX;
  const leftEdge = -offsetX - buffer;
  const rightEdge = viewportWidth - offsetX + buffer;

  const startBand =
    Math.floor((leftEdge - layout.startX) / layout.repeatWidth) - 1;
  const endBand =
    Math.ceil((rightEdge - layout.startX) / layout.repeatWidth) + 1;

  return { startBand, endBand };
}

// ================= 圆角函数 =================
function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.lineTo(x + w - r, y);
  ctx.quadraticCurveTo(x + w, y, x + w, y + r);
  ctx.lineTo(x + w, y + h - r);
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
  ctx.lineTo(x + r, y + h);
  ctx.quadraticCurveTo(x, y + h, x, y + h - r);
  ctx.lineTo(x, y + r);
  ctx.quadraticCurveTo(x, y, x + r, y);
  ctx.closePath();
}

// ================= 绘制函数 =================
function drawGallery() {
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  ctx.fillStyle = "#1a1a1a";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  if (state.images.length === 0) {
    requestAnimationFrame(drawGallery);
    return;
  }

  const layout = getColumnLayout(canvas.width);
  const { startBand, endBand } = getVisibleBandRange(
    state.offsetX,
    canvas.width,
    layout,
  );

  for (let band = startBand; band <= endBand; band++) {
    for (let columnIndex = 0; columnIndex < layout.columnCount; columnIndex++) {
      const laneGapY = config.baseGapY + (columnIndex % 3) * 14;
      const horizontalShift =
        (columnIndex - (layout.columnCount - 1) / 2) *
        config.columnShiftX *
        0.45;
      const verticalShift =
        (columnIndex % 2) * Math.floor(config.imgHeight * 0.18);
      const stepY = config.imgHeight + laneGapY;

      const x =
        layout.startX +
        band * layout.repeatWidth +
        columnIndex * (config.imgWidth + layout.spacingX) +
        horizontalShift +
        state.offsetX;

      const scrollOffsetY = state.offsetY + verticalShift;
      const startRow =
        Math.floor((-scrollOffsetY - config.imgHeight) / stepY) - 1;
      const endRow = Math.ceil((canvas.height - scrollOffsetY) / stepY) + 1;

      for (let row = startRow; row <= endRow; row++) {
        const y = row * stepY + scrollOffsetY;
        const patternIndex =
          ((columnIndex % layout.columnCount) + layout.columnCount) %
          layout.columnCount;
        const index =
          (((row * 31 + patternIndex * 17 + band * 13) % state.images.length) +
            state.images.length) %
          state.images.length;

        const src = state.images[index];
        const img = loadImage(src);

        ctx.save();
        roundRect(
          ctx,
          x,
          y,
          config.imgWidth,
          config.imgHeight,
          config.cornerRadius,
        );
        ctx.clip();

        if (img.complete && img.naturalWidth !== 0) {
          drawImageCover(ctx, img, x, y, config.imgWidth, config.imgHeight);
        } else {
          ctx.fillStyle = "#ddd";
          ctx.fillRect(x, y, config.imgWidth, config.imgHeight);

          ctx.fillStyle = "#999";
          ctx.fillText("Loading...", x + 20, y + 30);
        }

        ctx.restore();
      }
    }
  }

  requestAnimationFrame(drawGallery);
}

// ================= 拖拽 =================
canvas.addEventListener("mousedown", (e) => {
  state.isDragging = true;
  state.lastMouse = { x: e.clientX, y: e.clientY };
});

window.addEventListener("mousemove", (e) => {
  if (!state.isDragging) return;

  state.offsetX += e.clientX - state.lastMouse.x;
  state.offsetY += e.clientY - state.lastMouse.y;

  state.lastMouse = { x: e.clientX, y: e.clientY };
});

window.addEventListener("mouseup", () => {
  state.isDragging = false;
});

// ================= 启动 =================
initImages();
drawGallery();
