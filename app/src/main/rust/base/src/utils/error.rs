use jni::JNIEnv;

pub trait JNIErr<T> {
    fn unwrap_jni(self, env: &mut JNIEnv) -> T;
}

#[macro_export]
macro_rules! jni_try {
    ($env:expr, $expr:expr) => {
        match $expr {
            Ok(v) => v,
            Err(e) => {
                let _ = $env.throw_new("java/lang/Exception", e.to_string());
                log::error!("{}", e);
                return; // early-return from the JNI function
            },
        }
    };
}

// impl<T, E: std::fmt::Display> JNIErr<T> for std::result::Result<T, E> {
//     fn unwrap_jni(self, env: &mut JNIEnv) -> T {
//         match self {
//             Ok(v) => v,
//             Err(e) => {
//                 let _ = env.throw_new("java/lang/Exception", e.to_string());
//             }
//         }
//     }
// }
