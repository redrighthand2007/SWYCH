import sys
import re

msg = sys.stdin.read().strip().split('\n')[0]
msg = re.sub(r'^(UI|UX|Fix|Update|Add|Delete|Refactor|Merge)[\s\:\-]*', '', msg, flags=re.IGNORECASE)
words = re.findall(r'[a-zA-Z0-9]+', msg.lower())

if not words:
    words = ['done', 'some', 'stuff']

new_msg = ' '.join(words[:3])

print(new_msg)
