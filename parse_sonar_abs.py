#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import re

# Read JSON file
with open('D:/python_project/xuanjiao2/sonar_issues.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

issues = data.get('issues', [])

# Filter OPEN status issues and extract required info
results = []
for issue in issues:
    if issue.get('status') != 'OPEN':
        continue

    component = issue.get('component', '')
    # Extract file name from component (remove xuanjiao-backend: prefix)
    parts = component.split(':')
    file_name = parts[-1] if len(parts) > 1 else component

    start_line = issue.get('textRange', {}).get('startLine', 0)
    message = issue.get('message', '')

    # Extract complexity from message (format: "from 194 to the 15 allowed")
    match = re.search(r'from (\d+) to', message)
    complexity = int(match.group(1)) if match else 0

    results.append({
        'complexity': complexity,
        'file': file_name,
        'line': start_line,
        'method': 'unknown'
    })

# Function to find method name at line number
def find_method_name(file_path, line_num):
    """Find method name containing the given line number."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        # Look backwards from the line to find method declaration
        method_pattern = re.compile(r'(?:public|private|protected)?\s*(?:static)?\s*(?:abstract)?\s*(?:[\w<>?,\s]+\s+)?(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w\s,]+)?\s*\{')

        for i in range(min(line_num, len(lines)), max(0, line_num - 50), -1):
            line = lines[i - 1].rstrip()
            match = method_pattern.search(line)
            if match:
                return match.group(1)
    except Exception as e:
        pass
    return 'unknown'

# Infer method names from source files
for r in results:
    # Convert to absolute path
    file_path = f'D:/python_project/xuanjiao2/{r["file"]}'
    r['method'] = find_method_name(file_path, r['line'])
    r['abs_path'] = file_path

# Sort by complexity descending
results.sort(key=lambda x: x['complexity'], reverse=True)

# Print top 10 results with absolute paths
print()
print('复杂度 | 文件 | 行号 | 方法')
print('-------|------|------|----------')
for i, r in enumerate(results[:10]):
    # Use absolute file path
    print(f"{r['complexity']:>6} | {r['abs_path']} | {r['line']:>4} | {r['method']}")
