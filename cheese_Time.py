import socketio
import time
from urllib.parse import urlencode

HOST = 'https://ucontent.unipus.cn'
UUID = '5d204e57c8904d9c9ffcbd8df9eba0b8'
TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcGVuX2lkIjoiNWQyMDRlNTdjODkwNGQ5YzlmZmNiZDhkZjllYmEwYjgiLCJuYW1lIjoiIiwiZW1haWwiOiIiLCJhZG1pbmlzdHJhdG9yIjpmYWxzZSwiZXhwIjoxNzk2ODI0NTM3MzQ1LCJpc3MiOiJjNGY3NzIwNjNkY2ZhOThlOWM1MCIsImF1ZCI6ImVkeC51bmlwdXMuY24ifQ.D17ClB4pdaLFihXsCDiyOc97ne8OrGwIzGboRRAimNs'

PAYLOAD = {
    'client': 'pc',
    'module': '70bb13785202ab2',
    'moduleGroup': 'course-v2:8153edc7900eda3+nhce_4_vls_xxt_1+20250620',
    'url': 'https://ucontent.unipus.cn/_explorationpc_default/pc.html?cid=1585602187982291184&theme=3264FA&aitutorialId=19825&cloudCurriculaId=216914&source=cloud&courseResourceId=20000804842#/course-v2:8153edc7900eda3+nhce_4_vls_xxt_1+20250620/courseware/741135b5a002c50/70bb13785182ab2/70bb13785192ab2/70bb13785202ab2',
    'tag1': '741135b5a002c50',
    'tag2': '70bb13785182ab2',
    'tag3': '{"microBlock":"741135b5a002c50/70bb13785182ab2","version":"1","source":"ucontent"}'
}


def on_useractivities_connect():
    p = dict(PAYLOAD)
    p['timer'] = int(time.time() * 1000)
    sio.emit('start', p, namespace='/userActivities', callback=lambda ack: print('start ack:', ack))

def stop_now():
    p = dict(PAYLOAD)
    p['timer'] = int(time.time() * 1000)
    sio.emit('stop', p, namespace='/userActivities', callback=lambda ack: print('stop ack:', ack))

qs = urlencode({'uuid': UUID, 'token': TOKEN})

while True:
    try:
        sio = socketio.Client(logger=True, engineio_logger=True)
        sio.on('connect', namespace='/userActivities')(on_useractivities_connect)
        sio.connect(f'{HOST}?{qs}',
                    socketio_path='/unipusio',
                    transports=['websocket'],
                    namespaces=['/userActivities'])
        time.sleep(60 * 60)
        stop_now()
        try:
            sio.disconnect()
        except Exception as e:
            print('disconnect error:', e)
    except KeyboardInterrupt:
        try:
            sio.disconnect()
        except Exception as e:
            print('disconnect error:', e)
        break
    except Exception as e:
        print('connect loop error:', e)
        try:
            sio.disconnect()
        except Exception:
            pass
        time.sleep(10)
    time.sleep(5)


