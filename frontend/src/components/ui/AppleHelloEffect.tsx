import { motion } from "motion/react";

const initialProps = { pathLength: 0, opacity: 0 } as const;
const animateProps = { pathLength: 1, opacity: 1 } as const;

type Props = React.ComponentProps<typeof motion.svg> & {
    speed?: number;
    onAnimationComplete?: () => void;
};

export function AppleHelloEnglishEffect({
                                            className,
                                            speed = 1,
                                            onAnimationComplete,
                                            ...props
                                        }: Props) {
    const calc = (x: number) => x * speed;

    return (
        <motion.svg
            className={className}
            style={{
                color: 'white',
                height: '16rem',
                width: 'auto',
                maxWidth: '900px'
            }}
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 920 120"
            fill="none"
            stroke="currentColor"
            strokeWidth="6"
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.5 }}
            {...props}
        >

            <title>Welcome to Messenger</title>
            {/* W */}
            <motion.path
                d="M15 30 L30 85 L45 50 L60 85 L75 30"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.3), ease: "easeInOut" }}
            />
            {/* e */}
            <motion.path
                d="M85 62 Q85 44 103 44 Q121 44 121 62 L85 62 Q85 89 114 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(0.25) }}
            />
            {/* l */}
            <motion.path
                d="M140 25 L140 85"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.15), ease: "easeInOut", delay: calc(0.45) }}
            />
            {/* c */}
            <motion.path
                d="M181 47 Q156 45 156 65 Q156 85 181 85"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.2), ease: "easeInOut", delay: calc(0.55) }}
            />
            {/* o */}
            <motion.path
                d="M196 65 Q196 45 211 45 Q226 45 226 65 Q226 85 211 85 Q196 85 196 65"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(0.7) }}
            />
            {/* m */}
            <motion.path
                d="M244 85 L244 55 Q244 45 259 45 Q269 45 269 55 L269 85 M269 55 Q269 45 284 45 Q294 45 294 55 L294 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.3), ease: "easeInOut", delay: calc(0.9) }}
            />
            {/* e */}
            <motion.path
                d="M309 62 Q309 44 327 44 Q345 44 345 62 L309 62 Q309 89 338 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(1.15) }}
            />
            {/* t */}
            <motion.path
                d="M385 30 L385 85 M370 45 L400 45"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.2), ease: "easeInOut", delay: calc(1.4) }}
            />
            {/* o */}
            <motion.path
                d="M410 65 Q410 45 425 45 Q440 45 440 65 Q440 85 425 85 Q410 85 410 65"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(1.55) }}
            />
            {/* M */}
            <motion.path
                d="M482 85 L482 30 L507 60 L532 30 L532 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.35), ease: "easeInOut", delay: calc(1.8) }}
            />
            {/* e */}
            <motion.path
                d="M547 60 Q547 42 565 42 Q583 42 583 60 L547 60 Q547 87 576 83"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(2.1) }}
            />
            {/* s */}
            <motion.path
                d="M620 44 Q600 41 600 54 Q599 64 609 64 Q624 63 624 75 Q624 88 604 85"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(2.3) }}
            />
            {/* s */}
            <motion.path
                d="M654 44 Q634 41 634 54 Q633 64 643 64 Q658 63 658 75 Q658 88 638 85"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(2.5) }}
            />
            {/* e */}
            <motion.path
                d="M674 62 Q674 44 692 44 Q710 44 710 62 L674 62 Q674 89 703 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(2.7) }}
            />
            {/* n */}
            <motion.path
                d="M725 85 L725 50 Q725 45 740 45 Q755 45 755 55 L755 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(2.9) }}
            />
            {/* g */}
            <motion.path
                d="M790 45 Q770 45 770 60 Q770 75 790 75 L790 45 L790 95 Q790 110 770 105"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.3), ease: "easeInOut", delay: calc(3.1) }}
            />
            {/* e */}
            <motion.path
                d="M807 62 Q807 44 825 44 Q843 44 843 62 L807 62 Q807 89 836 85"
                style={{ strokeLinecap: "round", strokeLinejoin: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.25), ease: "easeInOut", delay: calc(3.35) }}
            />
            {/* r */}
            <motion.path
                d="M860 85 L860 55 Q860 45 875 45"
                style={{ strokeLinecap: "round" }}
                initial={initialProps}
                animate={animateProps}
                transition={{ duration: calc(0.2), ease: "easeInOut", delay: calc(3.55) }}
                onAnimationComplete={onAnimationComplete}
            />
        </motion.svg>
    );
}
