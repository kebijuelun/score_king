#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Create app icon for the score game
为计分游戏创建应用图标
"""

from PIL import Image, ImageDraw, ImageFont
import os

def create_app_icon():
    """Create a simple app icon"""
    # Create a 512x512 image with gradient background
    size = 512
    img = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    draw = ImageDraw.Draw(img)
    
    # Draw gradient background
    for i in range(size):
        color_value = int(255 * (1 - i / size))
        color = (66, 126, 234, 255)  # Blue gradient
        draw.line([(0, i), (size, i)], fill=color)
    
    # Draw circular background
    margin = 50
    circle_bbox = [margin, margin, size - margin, size - margin]
    draw.ellipse(circle_bbox, fill=(255, 255, 255, 240), outline=(66, 126, 234, 255), width=8)
    
    # Draw game elements
    center_x, center_y = size // 2, size // 2
    
    # Draw score display (rectangle)
    score_rect = [center_x - 80, center_y - 120, center_x + 80, center_y - 60]
    draw.rectangle(score_rect, fill=(40, 167, 69, 255), outline=(33, 136, 56, 255), width=3)
    
    # Draw players (circles)
    player_radius = 25
    player_positions = [
        (center_x - 60, center_y + 20),
        (center_x + 60, center_y + 20),
        (center_x, center_y + 80)
    ]
    
    colors = [(220, 53, 69, 255), (255, 193, 7, 255), (23, 162, 184, 255)]
    
    for i, (x, y) in enumerate(player_positions):
        draw.ellipse([x - player_radius, y - player_radius, 
                     x + player_radius, y + player_radius], 
                    fill=colors[i], outline=(255, 255, 255, 255), width=3)
    
    # Try to add text
    try:
        # Use a default font or system font
        font_size = 40
        try:
            font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", font_size)
        except:
            try:
                font = ImageFont.truetype("arial.ttf", font_size)
            except:
                font = ImageFont.load_default()
        
        # Draw title text
        text = "计分"
        bbox = draw.textbbox((0, 0), text, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
        text_x = (size - text_width) // 2
        text_y = center_y - 90
        
        draw.text((text_x, text_y), text, fill=(255, 255, 255, 255), font=font)
        
    except Exception as e:
        print(f"Font loading failed: {e}")
        # Draw simple shapes instead of text
        draw.rectangle([center_x - 30, center_y - 90, center_x + 30, center_y - 70], 
                      fill=(255, 255, 255, 255))
    
    return img

def main():
    """Main function to create icons"""
    print("🎨 正在创建应用图标...")
    
    # Create data directory
    os.makedirs('data', exist_ok=True)
    
    # Create main icon
    icon = create_app_icon()
    
    # Save different sizes
    sizes = [512, 256, 128, 72, 48, 36]
    
    for size in sizes:
        resized_icon = icon.resize((size, size), Image.Resampling.LANCZOS)
        filename = f'data/icon-{size}.png'
        resized_icon.save(filename)
        print(f"✅ 已创建: {filename}")
    
    # Save main icon
    icon.save('data/icon.png')
    print("✅ 已创建: data/icon.png")
    
    # Create adaptive icon components for Android
    # Foreground (icon content)
    fg_size = 432  # 108dp * 4 for xxxhdpi
    fg_icon = icon.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
    
    # Create transparent background with icon in center
    adaptive_fg = Image.new('RGBA', (512, 512), (255, 255, 255, 0))
    fg_x = (512 - fg_size) // 2
    fg_y = (512 - fg_size) // 2
    adaptive_fg.paste(fg_icon, (fg_x, fg_y))
    adaptive_fg.save('data/icon_fg.png')
    print("✅ 已创建: data/icon_fg.png")
    
    # Background (solid color)
    adaptive_bg = Image.new('RGBA', (512, 512), (66, 126, 234, 255))
    adaptive_bg.save('data/icon_bg.png')
    print("✅ 已创建: data/icon_bg.png")
    
    print("\n🎉 所有图标文件创建完成！")

if __name__ == '__main__':
    main()