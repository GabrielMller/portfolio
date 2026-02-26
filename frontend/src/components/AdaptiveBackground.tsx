'use client';
import { Box, useColorScheme, useTheme } from "@mui/material";
import { FC, useEffect, useRef } from "react";

class Particle {
  x: number = 0; y: number = 0; size: number = 0;
  opacity: number = 0; blinkSpeed: number = 0;
  canvas: HTMLCanvasElement;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.reset();
  }

  reset(): void {
    this.x = Math.random() * this.canvas.width;
    this.y = Math.random() * this.canvas.height;
    this.size = Math.random() * 1.5;
    this.opacity = Math.random();
    this.blinkSpeed = 0.003 + Math.random() * 0.001;
  }

  draw(ctx: CanvasRenderingContext2D, channel: string, isLight: boolean): void {
    this.opacity += this.blinkSpeed;
    if (this.opacity > 1 || this.opacity < 0) this.blinkSpeed *= -1;
    
    const drawSize = isLight ? this.size * 2.5 : this.size;
    
    ctx.fillStyle = `rgba(${channel} / ${Math.abs(this.opacity)})`;
    ctx.beginPath();
    ctx.arc(this.x, this.y, drawSize, 0, Math.PI * 2);
    ctx.fill();
    
    if (isLight) {
        ctx.shadowBlur = 8;
        ctx.shadowColor = `rgba(${channel} / 0.4)`;
    } else {
        ctx.shadowBlur = 0;
    }
  }
}

class Streak {
  x: number = 0; y: number = 0; length: number = 0;
  speed: number = 0; opacity: number = 0; width: number = 0;
  active: boolean = false; canvas: HTMLCanvasElement;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.reset();
  }

  reset(): void {
    this.x = Math.random() * this.canvas.width + 300;
    this.y = Math.random() * this.canvas.height * 0.4 - 50;
    this.length = 100 + Math.random() * 150;
    this.speed = 8 + (Math.random() * 3);
    this.opacity = 1;
    this.width = 2;
    this.active = false;
  }

  launch(): void { this.active = true; }

  draw(ctx: CanvasRenderingContext2D, channel: string, isLight: boolean): void {
    if (!this.active) return;
    ctx.save();
    
    const displayLength = isLight ? this.length * 0.4 : this.length;
    
    ctx.strokeStyle = `rgba(${channel} / ${this.opacity})`;
    ctx.lineWidth = isLight ? this.width * 0.5 : this.width;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(this.x, this.y);
    ctx.lineTo(this.x - displayLength, this.y + displayLength);
    ctx.stroke();
    ctx.restore();

    this.x -= this.speed;
    this.y += this.speed;
    this.opacity -= isLight ? 0.04 : 0.015;

    if (this.opacity <= 0 || this.y > this.canvas.height) {
      this.active = false;
      this.reset();
    }
  }
}

const AdaptiveBackground: FC = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const { mode } = useColorScheme();

  useEffect(() => {
    async function request() {
      const data = await fetch('http://localhost:8081/api/health/v1/kafka', {
        headers: { 'Content-Type': 'application/json' },
        method: 'GET',
      });
      console.log(await data.json());
    }
    request();
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    const particles: Particle[] = [];
    const streaks: Streak[] = [];
    const particleCount = mode === 'light' ? 50 : 130;
    let lastStreakTimestamp = 0;
    const STREAK_INTERVAL = mode === 'light' ? 5000 : 2500;
    
    const resizeCanvas = (): void => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };

    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();

    for (let i = 0; i < particleCount; i++) particles.push(new Particle(canvas));
    for (let i = 0; i < 3; i++) streaks.push(new Streak(canvas));

    const animate = (timestamp: number): void => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      
      const isLight = mode === 'light';
      const rootStyle = getComputedStyle(document.documentElement);
      
      const pChannel = rootStyle.getPropertyValue('--mui-palette-cosmic-particleChannel').trim() || '255 255 255';
      const sChannel = rootStyle.getPropertyValue('--mui-palette-cosmic-streakChannel').trim() || (isLight ? '255 255 255' : '96 165 250');

      if (timestamp - lastStreakTimestamp > STREAK_INTERVAL) {
        const inactive = streaks.find(m => !m.active);
        if (inactive) {
          inactive.launch();
          lastStreakTimestamp = timestamp;
        }
      }

      particles.forEach(p => p.draw(ctx, pChannel, isLight));
      streaks.forEach(s => s.draw(ctx, sChannel, isLight));
      
      animationFrameId = requestAnimationFrame(animate);
    };

    animationFrameId = requestAnimationFrame(animate);

    return () => {
      window.removeEventListener('resize', resizeCanvas);
      cancelAnimationFrame(animationFrameId);
    };
  }, [mode]);

  return (
    <Box sx={{ 
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
      zIndex: -1, 
      // Definição direta do fundo com base no modo para garantir atualização
      background: mode === 'light' 
        ? 'linear-gradient(180deg, #7dd3fc 0%, #e0f2fe 100%)' 
        : 'radial-gradient(circle at 50% 50%, #0f172a 0%, #020617 100%)',
      transition: 'background 0.8s cubic-bezier(0.4, 0, 0.2, 1)'
    }}>
      <canvas ref={canvasRef} style={{ display: 'block', width: '100%', height: '100%' }} />
    </Box>
  );
};

export default AdaptiveBackground;