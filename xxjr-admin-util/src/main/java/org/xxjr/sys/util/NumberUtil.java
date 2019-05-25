package org.xxjr.sys.util;

import java.math.BigDecimal;

import org.springframework.util.Assert;

public class NumberUtil {
	public static final byte getByte(Object value, byte defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).byteValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Byte.parseByte(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final short getShort(Object value) {
		Assert.notNull(value, "将要转换为整数的值不能为null!");
		return (value instanceof Number) ? ((Number) value).shortValue()
				: Short.parseShort(value.toString());
	}

	public static final short getShort(Object value, short defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).shortValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Short.parseShort(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final int getInt(Object value) {
		Assert.notNull(value, "将要转换为整数的值不能为null!");
		return (value instanceof Number) ? ((Number) value).intValue()
				: Integer.parseInt(value.toString());
	}

	public static final int getInt(Object value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).intValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final long getLong(Object value) {
		Assert.notNull(value, "将要转换为整数的值不能为null!");
		return (value instanceof Number) ? ((Number) value).longValue() : Long
				.parseLong(value.toString());
	}

	public static final long getLong(Object value, long defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).longValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Long.parseLong(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final float getFloat(Object value) {
		Assert.notNull(value, "将要转换为整数或小数的值不能为null!");
		return (value instanceof Number) ? ((Number) value).floatValue()
				: Float.parseFloat(value.toString());
	}

	public static final float getFloat(Object value, float defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).floatValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Float.parseFloat(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final double getDouble(Object value) {
		Assert.notNull(value, "将要转换为整数或小数的值不能为null!");
		return (value instanceof Number) ? ((Number) value).doubleValue()
				: Double.parseDouble(value.toString());
	}

	public static final double getDouble(Object value, double defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof Number))
			return ((Number) value).doubleValue();
		if ("".equals(value))
			return defaultValue;
		try {
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final BigDecimal getBigDecimal(Object value) {
		Assert.notNull(value, "将要转换为整数或小数的值不能为null!");
		if ((value instanceof BigDecimal))
			return (BigDecimal) value;
		if ((value instanceof Number)) {
			return BigDecimal.valueOf(((Number) value).doubleValue());
		}
		return new BigDecimal(value.toString());
	}

	public static final BigDecimal getBigDecimal(Object value,
			BigDecimal defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ((value instanceof BigDecimal))
			return (BigDecimal) value;
		if ((value instanceof Number))
			try {
				return BigDecimal.valueOf(((Number) value).doubleValue());
			} catch (Exception e) {
				return defaultValue;
			}
		try {
			return new BigDecimal(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static final BigDecimal getBigDecimal(Object value,
			String defaultValue) {
		if (value == null) {
			return new BigDecimal(defaultValue);
		}
		if ((value instanceof BigDecimal))
			return (BigDecimal) value;
		if ((value instanceof Number))
			try {
				return BigDecimal.valueOf(((Number) value).doubleValue());
			} catch (Exception e) {
				return new BigDecimal(defaultValue);
			}
		try {
			return new BigDecimal(value.toString());
		} catch (Exception e) {
		}
		return new BigDecimal(defaultValue);
	}

	public static final boolean isNumber(String str) {
		if (str == null) {
			return false;
		}
		char[] chars = str.toCharArray();
		if (chars.length < 1) {
			return false;
		}
		for (char c : chars) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumber(String str, int length) {
		if (length < 0) {
			throw new IllegalArgumentException("指定长度不能小于0!");
		}
		return (str != null) && (str.length() == length) && (isNumber(str));
	}

	public static boolean isNumber(Object obj) {
		if ((obj instanceof Number)) {
			Number num = (Number) obj;
			return num.longValue() == num.doubleValue();
		}
		return (obj != null) && (isNumber(obj.toString()));
	}

	public static final boolean isNumeric(String str) {
		int length;
		if ((str == null) || ((length = str.length()) == 0))
			return false;
		char[] chars = str.toCharArray();
		do {
			if ((chars[(--length)] < '0') || (chars[length] > '9'))
				return false;
		} while (length > 0);
		return true;
	}

	public static final boolean isInt(Object value) {
		return (value != null)
				&& (((value instanceof Integer)) || (isNumber(value.toString())));
	}

	public static final boolean isDouble(String str) {
		if (str == null) {
			return false;
		}
		int pointPos = str.indexOf('.', 0) + 1;
		if (pointPos == str.length())
			return false;
		if (pointPos > 0) {
			return (isNumeric(str.substring(0, pointPos - 1)))
					&& (isNumeric(str.substring(pointPos)));
		}
		return isNumeric(str);
	}

	public static final boolean isDouble(Object value) {
		return (value != null)
				&& (((value instanceof Number)) || (isDouble(value.toString())));
	}
}