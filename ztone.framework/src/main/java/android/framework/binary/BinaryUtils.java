package android.framework.binary;

import android.assist.Assert;
import android.concurrent.ThreadUtils;
import android.log.Log;
import android.math.BCC;
import android.math.Maths;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static android.framework.binary.BinaryProtocol.SESSION_NONE;
import static android.framework.binary.ResultProtocol.FLAG_BCC_ERROR;
import static android.framework.binary.ResultProtocol.FLAG_DOING;
import static android.framework.binary.ResultProtocol.FLAG_END;
import static android.framework.binary.ResultProtocol.FLAG_ERROR;
import static android.framework.binary.ResultProtocol.FLAG_FINISH;
import static android.framework.binary.ResultProtocol.FLAG_LEN_ERROR;
import static android.framework.binary.ResultProtocol.FLAG_NONE;
import static android.framework.binary.ResultProtocol.FLAG_TEXT_ERROR;

public class BinaryUtils {
	private static final String TAG = "BinaryUtils";

	private static final int MICRO_SO_TIMEOUT = 15000;
	private static final int MICRO_SOCKET_BUFFER = 128;

	public static final int PART_START = 0;
	public static final int PART_SESSION = PART_START + 1;
	public static final int PART_LEN = PART_SESSION + 1;
	public static final int PART_TEXT = PART_LEN + 1;
	public static final int PART_BCC = PART_TEXT + 1;
	public static final int PART_END = PART_BCC + 1;

	public static final int LEN_BYTE = 8;
	public static final int LEN_START = 2;
	public static final int LEN_LEN = 2;
	public static final int LEN_END = 2;
	public static final int LEN_CODE = 2;

	public static final String RST_SUCCESS = "9000";
	public static final String RST_ERROR_AUTHORITY = "9301";

	public static final String FORMAT_LENGHT = "%04x";

	protected static final byte[] PROT_REQUEST_START = { 0x05, 0x02 }, PROT_RESPONSE_START = { 0x06, 0x02 };
	protected static final byte[] PROT_END = { 0x03, 0x04 };

	protected static final int REQUEST_START = (PROT_REQUEST_START[0] << LEN_BYTE) | PROT_REQUEST_START[1];
	protected static final int RESPONSE_START = (PROT_RESPONSE_START[0] << LEN_BYTE) | PROT_RESPONSE_START[1];
	protected static final int END = (PROT_END[0] << LEN_BYTE) | PROT_END[1];

	private static final Handler mHandler = new Handler(Looper.getMainLooper());

	public enum ConnectType {
		REQUEST, RESPONSE
	}

	public interface OnBinaryPulledListener {

		void onPulled(ResultProtocol resultProtocol);
	}

	public static void pullData(final String ip, final int port, final CommandProtocol commandProtocol,
			final OnBinaryPulledListener binaryPulledListener) {
		ThreadUtils.start(new Runnable() {

			@Override
			public void run() {
				final ResultProtocol resultProtocol = getData(ip, port, commandProtocol);
				if (mHandler != null) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							if (binaryPulledListener != null) {
								binaryPulledListener.onPulled(resultProtocol);
							}
						}
					});
				}
			}

		}, "BINARY_PULL_DATA");
	}

	public static ResultProtocol getData(String ip, int port, CommandProtocol commandProtocol) {
		ResultProtocol resultProtocol = null;

		if (Assert.notEmpty(ip) && port != 0 && commandProtocol != null) {
			long now = SystemClock.uptimeMillis();

			Socket socket = null;
			InputStream in = null;
			BufferedOutputStream out = null;

			try {
				socket = new Socket(ip, port);
				socket.setSoTimeout(MICRO_SO_TIMEOUT);
				socket.setReceiveBufferSize(MICRO_SOCKET_BUFFER);

				out = new BufferedOutputStream(socket.getOutputStream());
				BinaryUtils.write(ConnectType.REQUEST, commandProtocol, out);

				out.flush();

				in = new BufferedInputStream(socket.getInputStream(), MICRO_SOCKET_BUFFER);
				resultProtocol = BinaryUtils.read(in);

				Log.d(TAG, "BP: NN: t: %1$d, p: %2$s", (SystemClock.uptimeMillis() - now), //
						(resultProtocol != null ? resultProtocol.toString() : "NULL"));
			} catch (Exception e) {
				Log.e(e);

			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						Log.v(e);
					}
				}

				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
						Log.v(e);
					}
				}

				if (socket != null && !socket.isClosed()) {
					try {
						socket.close();
					} catch (Exception e) {
						Log.v(e);
					}
				}
			}
		}

		return resultProtocol;
	}

	public static void write(ConnectType type, CommandProtocol protocol, OutputStream out) {
		if (protocol != null && protocol.check() && out != null && type != null) {
			try {
				// START
				out.write(type == ConnectType.RESPONSE ? PROT_RESPONSE_START : PROT_REQUEST_START);

				// SESSION
				out.write(new byte[] { protocol.session == SESSION_NONE ? protocol.nextSession() : protocol.session });

				// 组装TEXT
				protocol.fitted();
				if (Assert.notEmpty(protocol.content)) {
					// LEN
					out.write(Maths.toByte(String.format(FORMAT_LENGHT, (protocol.code.length() >> 1)
							+ (protocol.content.length() >> 1))));

					// CODE
					out.write(Maths.toByte(protocol.code));

					// CONTENT
					out.write(Maths.toByte(protocol.content));
				} else {
					// LEN
					out.write(Maths.toByte(String.format(FORMAT_LENGHT, (protocol.code.length() >> 1))));

					// CODE
					out.write(Maths.toByte(protocol.code));
				}

				// BCC
				out.write(new byte[] { (byte) (BCC.encrypt(protocol.code) ^ BCC.encrypt(protocol.content)) });

				// END
				out.write(PROT_END);
			} catch (Exception e) {
				Log.e(TAG, e);
			}
		}
	}

	public static final ResultProtocol read(InputStream in) {
		ResultProtocol protocol = new ResultProtocol();

		protocol.mStep = FLAG_NONE;

		if (in != null) {
			int len = 0, d = 0, temp = 0x00;
			int part = 0;
			byte[] tbyte = new byte[1];
			int dataLen = 0;

			byte bcc = 0x00;

			final StringBuilder sbCMD = new StringBuilder(), log = new StringBuilder();

			try {
				while ((d = in.read()) != -1) {
					final byte bt = (byte) (d & 0xFF);

					if (protocol.mStep == FLAG_NONE) {
						protocol.mStep = FLAG_DOING;
					}

					log.append(Maths.toHex(new byte[] { bt }));

					// 长度
					len++;
					temp = (temp << 8) + bt;

					if (part == PART_START) {
						if (len == LEN_START) {
							if (temp == RESPONSE_START) {
								protocol.mStep = FLAG_END;

								// 异常
							} else {
								protocol.mStep = FLAG_ERROR;

								break;
							}
						}
					} else if (part == PART_SESSION) {
						protocol.session = bt;

						protocol.mStep = FLAG_END;
					} else if (part == PART_LEN) {
						if (len == LEN_LEN) {
							protocol.mStep = FLAG_END;

							dataLen = temp;
							if (dataLen == 0) {
								protocol.mStep = FLAG_LEN_ERROR;

								break;
							}
						}

					} else if (part == PART_TEXT) {
						tbyte[0] = bt;
						sbCMD.append(Maths.toHex(tbyte));

						if (len == dataLen) {
							protocol.mStep = FLAG_END;

							if (sbCMD.length() <= LEN_CODE) {
								protocol.mStep = FLAG_TEXT_ERROR;

								break;
							}
						}
					} else if (part == PART_BCC) {
						bcc = bt;

						protocol.mStep = FLAG_END;
					} else if (part == PART_END) {
						// 解析結束
						if (len == LEN_END) {
							protocol.mStep = temp == END ? FLAG_FINISH : FLAG_ERROR;

							break;
						}
					}

					if (protocol.mStep == FLAG_END) {
						temp = 0x00;
						len = 0;

						part++;

						protocol.mStep = FLAG_NONE;
					}
				}

				if (protocol.mStep == FLAG_FINISH) {
					if (bcc == BCC.encrypt(sbCMD.toString())) {
						int slen = LEN_CODE << 1;
						protocol.code = sbCMD.substring(0, slen);
						protocol.content = sbCMD.substring(slen);
					} else {
						protocol.mStep = FLAG_BCC_ERROR;
					}

				}
			} catch (Exception e) {
				Log.e(e);
			}

			protocol.mCommandText = log.toString();
		}

		return protocol;
	}
}
