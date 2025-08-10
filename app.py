#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Multi-player Score Tracking Game
支持多人多轮计分的网页游戏
- Multiple players support
- Cumulative scoring across rounds
- Configurable win threshold (default: 200)
- Reset functionality
- Real-time winner detection
"""

from flask import Flask, render_template, request, jsonify
import json
import os

app = Flask(__name__)

# Global game state
game_state = {
    'players': {},  # {player_name: {'score': total_score, 'rounds': [round_scores]}}
    'win_threshold': 200,
    'winner': None,
    'game_over': False
}

@app.route('/')
def index():
    """Main game page"""
    return render_template('index.html')

@app.route('/api/add_player', methods=['POST'])
def add_player():
    """Add a new player to the game"""
    data = request.get_json()
    player_name = data.get('name', '').strip()
    
    if not player_name:
        return jsonify({'error': '玩家名称不能为空'}), 400
    
    if player_name in game_state['players']:
        return jsonify({'error': '玩家已存在'}), 400
    
    game_state['players'][player_name] = {
        'score': 0,
        'rounds': []
    }
    
    return jsonify({
        'success': True,
        'players': game_state['players'],
        'message': f'玩家 {player_name} 已添加'
    })

@app.route('/api/add_score', methods=['POST'])
def add_score():
    """Add score for a player in current round"""
    data = request.get_json()
    player_name = data.get('player')
    round_score = data.get('score', 0)
    
    if player_name not in game_state['players']:
        return jsonify({'error': '玩家不存在'}), 400
    
    try:
        round_score = int(round_score)
    except (ValueError, TypeError):
        return jsonify({'error': '分数必须是数字'}), 400
    
    # Add score to player
    game_state['players'][player_name]['score'] += round_score
    game_state['players'][player_name]['rounds'].append(round_score)
    
    # Check for winner
    if game_state['players'][player_name]['score'] >= game_state['win_threshold']:
        game_state['winner'] = player_name
        game_state['game_over'] = True
    
    return jsonify({
        'success': True,
        'players': game_state['players'],
        'winner': game_state['winner'],
        'game_over': game_state['game_over']
    })

@app.route('/api/set_threshold', methods=['POST'])
def set_threshold():
    """Set winning score threshold"""
    data = request.get_json()
    threshold = data.get('threshold')
    
    try:
        threshold = int(threshold)
        if threshold <= 0:
            raise ValueError
    except (ValueError, TypeError):
        return jsonify({'error': '阈值必须是正整数'}), 400
    
    game_state['win_threshold'] = threshold
    game_state['winner'] = None
    game_state['game_over'] = False
    
    # Recheck for winners with new threshold
    for player_name, player_data in game_state['players'].items():
        if player_data['score'] >= threshold:
            game_state['winner'] = player_name
            game_state['game_over'] = True
            break
    
    return jsonify({
        'success': True,
        'win_threshold': game_state['win_threshold'],
        'winner': game_state['winner'],
        'game_over': game_state['game_over']
    })

@app.route('/api/reset_game', methods=['POST'])
def reset_game():
    """Reset the entire game"""
    game_state['players'] = {}
    game_state['winner'] = None
    game_state['game_over'] = False
    
    return jsonify({
        'success': True,
        'message': '游戏已重置',
        'players': game_state['players']
    })

@app.route('/api/reset_scores', methods=['POST'])
def reset_scores():
    """Reset all player scores but keep players"""
    for player_data in game_state['players'].values():
        player_data['score'] = 0
        player_data['rounds'] = []
    
    game_state['winner'] = None
    game_state['game_over'] = False
    
    return jsonify({
        'success': True,
        'message': '所有分数已清零',
        'players': game_state['players']
    })

@app.route('/api/remove_player', methods=['POST'])
def remove_player():
    """Remove a player from the game"""
    data = request.get_json()
    player_name = data.get('player')
    
    if player_name not in game_state['players']:
        return jsonify({'error': '玩家不存在'}), 400
    
    del game_state['players'][player_name]
    
    # Reset winner if removed player was the winner
    if game_state['winner'] == player_name:
        game_state['winner'] = None
        game_state['game_over'] = False
        # Recheck for winners
        for name, player_data in game_state['players'].items():
            if player_data['score'] >= game_state['win_threshold']:
                game_state['winner'] = name
                game_state['game_over'] = True
                break
    
    return jsonify({
        'success': True,
        'players': game_state['players'],
        'winner': game_state['winner'],
        'game_over': game_state['game_over']
    })

@app.route('/api/game_state')
def get_game_state():
    """Get current game state"""
    return jsonify({
        'players': game_state['players'],
        'win_threshold': game_state['win_threshold'],
        'winner': game_state['winner'],
        'game_over': game_state['game_over']
    })

if __name__ == '__main__':
    # Create templates directory if it doesn't exist
    os.makedirs('templates', exist_ok=True)
    
    app.run(debug=True, host='0.0.0.0', port=8080)
